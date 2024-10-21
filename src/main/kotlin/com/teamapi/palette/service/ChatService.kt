package com.teamapi.palette.service

import com.azure.ai.openai.models.*
import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceAsyncClient
import com.teamapi.palette.service.infra.comfy.GenerateRequest
import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.entity.qna.ChatAnswer
import com.teamapi.palette.entity.qna.PromptData
import com.teamapi.palette.entity.qna.QnA
import com.teamapi.palette.repository.chat.ChatRepository
import com.teamapi.palette.repository.qna.QnARepository
import com.teamapi.palette.repository.room.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.service.infra.ChatEmitService
import com.teamapi.palette.service.infra.GenerativeChatService
import com.teamapi.palette.service.infra.GenerativeImageService
import com.teamapi.palette.service.infra.comfy.ws.GenerateMessage
import com.teamapi.palette.service.infra.comfy.ws.QueueInfoMessage
import com.teamapi.palette.ws.actor.SinkActor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val qnaRepository: QnARepository,
    private val chatEmitService: ChatEmitService,
    private val actor: SinkActor,
    private val sessionHolder: SessionHolder,
    private val roomRepository: RoomRepository,
    private val generativeChatService: GenerativeChatService,
    private val generativeImageService: GenerativeImageService,
    private val blob: BlobServiceAsyncClient
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    private fun getPendingQuestion(qnA: QnA): PromptData? {
        return qnA.qna.find { it.answer == null }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun <T : ChatAnswer> createChat(roomId: Long, message: T) {
        val userId = sessionHolder.me()
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        var toBeResolved = qnaRepository.getQnAByRoomId(room.id!!)!!
        val pendingQuestion = getPendingQuestion(toBeResolved) ?: throw CustomException(ErrorCode.QNA_INVALID_FULFILLED)
        if (pendingQuestion.type != message.type)
            throw CustomException(ErrorCode.MESSAGE_TYPE_NOT_MATCH, message.type.name, pendingQuestion.type.name)

        when (message) {
            is ChatAnswer.SelectableAnswer -> {
                val selectable = pendingQuestion as PromptData.Selectable
                if (selectable.question.choices.none { it.id == message.choiceId })
                    throw CustomException(
                        ErrorCode.QNA_INVALID_CHOICES,
                        message.choiceId,
                        selectable.question.choices.joinToString(", ") { "'${it.id}'" }
                    )

                chatEmitService.emitChat(
                    Chat(
                        message = selectable.question.choices.find { it.id == message.choiceId }!!.displayName,
                        roomId = room.id,
                        userId = userId,
                        isAi = false
                    )
                )
            }

            is ChatAnswer.GridAnswer -> {
                val grid = pendingQuestion as PromptData.Grid
                val gridPossibleMax = grid.question.xSize * grid.question.ySize
                val maxSize = grid.question.maxCount
                val exceeds = message.choice.filter { it >= gridPossibleMax }
                if (exceeds.isNotEmpty())
                    throw CustomException(
                        ErrorCode.QNA_INVALID_GRID_CHOICES,
                        exceeds.joinToString(", "),
                        gridPossibleMax - 1
                    )

                if (message.choice.size > maxSize)
                    throw CustomException(ErrorCode.QNA_INVALID_GRID_ABOVE_MAX, maxSize)

                if (message.choice.size != message.choice.distinct().size)
                    throw CustomException(ErrorCode.QNA_INVALID_GRID_DUPE, (message.choice.withIndex().let {
                        @Suppress("ConvertArgumentToSet")
                        it - it.distinctBy { v -> v.value } // distinct is same with toSet
                    }).joinToString(", ") { "${it.value}" })


                chatEmitService.emitChat(
                    Chat(
                        message = message.choice.joinToString(", "),
                        roomId = room.id,
                        userId = userId,
                        isAi = false
                    )
                )
            }

            is ChatAnswer.UserInputAnswer -> {
                // TODO: Add length limit?

                chatEmitService.emitChat(
                    Chat(
                        message = message.input,
                        roomId = room.id,
                        userId = userId,
                        isAi = false
                    )
                )
            }

        }


        toBeResolved = qnaRepository.create(
            toBeResolved.copy(
                qna = toBeResolved.qna.map {
                    if (it.id == pendingQuestion.id) it.fulfillAnswer(message)
                    else it
                }
            )
        )

        CoroutineScope(Dispatchers.Unconfined).async {
            val pendingQnAs = toBeResolved.qna.filter { it.answer == null }
            if (pendingQnAs.isEmpty()) {
                val release = toBeResolved.qna

                val title = release.find { it.promptName == "title" }!!.answer as ChatAnswer.UserInputAnswer
                val explain = release.find { it.promptName == "product_explanation" }!!.answer as ChatAnswer.UserInputAnswer
                val aspectQnA = release.find { it.promptName == "aspect_ratio" }!! as PromptData.Selectable
                val aspectAns = aspectQnA.answer!!
                val hvQnA = release.find { it.promptName == "horizontal_or_vertical" }!! as PromptData.Selectable
                val hvAns = hvQnA.answer!!
                val grid = release.find { it.promptName == "title_position" }!!.answer as ChatAnswer.GridAnswer

                val userReturn = createUserReturn(explain.input).awaitSingle()
                chatEmitService.emitChat(
                    Chat(
                        resource = ChatState.CHAT,
                        roomId = room.id,
                        userId = userId,
                        isAi = true,
                        message = userReturn.choices.random().message.content
                    )
                )

                val (width, height) = when (aspectAns.choiceId) {
                    "DISPLAY" -> 1820 to 1024
                    "PAPER" -> 1444 to 1024
                    "SQUARE" -> 1024 to 1024
                    else -> 1365 to 1024 // TABLET
                }

                val prompt = createPrompt(explain.input).awaitSingle()
                val generated = generativeImageService.draw(
                    GenerateRequest(
                        title.input,
                        grid.choice[0],
                        if (hvAns.choiceId == "HORIZONTAL") width else height,
                        if (hvAns.choiceId == "HORIZONTAL") height else width,
                        prompt.choices.random().message.content
                    )
                )
                var guaranteed: String? = null
                generated.collect {
                    when (it) {
                        is QueueInfoMessage -> {
                            actor.addQueue(room.id, it.position)
                        }
                        is GenerateMessage -> {
                            if (it.result) {
                                guaranteed = it.image!!
                            }
                        }
                    }
                }
                if (guaranteed == null) {
                    chatEmitService.emitChat(
                        Chat(
                            resource = ChatState.CHAT,
                            roomId = room.id,
                            userId = userId,
                            isAi = true,
                            message = "이미지를 생성하는 도중 오류가 발생하였어요. ;.;"
                        )
                    )
                    return@async
                }

                try {
                    val space = blob.getBlobContainerAsyncClient("palette")
                    val blobClient = space.getBlobAsyncClient("${UUID.randomUUID()}.png")
                    blobClient.upload(BinaryData.fromBytes(Base64.decode(guaranteed!!))).awaitSingle()

                    chatEmitService.emitChat(
                        Chat(
                            resource = ChatState.IMAGE,
                            roomId = room.id,
                            userId = userId,
                            isAi = true,
                            message = blobClient.blobUrl
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val addResponse = pendingQnAs.first()
                val generated = generativeChatService.roomPromptMessage(addResponse.promptName)

                chatEmitService.emitChat(
                    Chat(
                        resource = ChatState.PROMPT,
                        roomId = room.id,
                        userId = userId,
                        isAi = true,
                        message = generated.choices.random().message.content,
                        promptId = addResponse.id
                    )
                )
            }
        }.invokeOnCompletion {
            it?.let {
                log.error("error", it)
            }
        }
    }

    suspend fun getChatList(roomId: Long, lastId: String, size: Long): List<ChatResponse> {
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        return chatRepository.getMessageByRoomId(
            room.id!!,
            Instant.parse(lastId),
            size
        )
    }

    fun createUserReturn(text: String): Mono<ChatCompletions> = generativeChatService.chatCompletion(
        ChatCompletionsOptions(
            listOf(
                ChatRequestSystemMessage(
                    "너는 세계에서 제일 가는 유명한 화가야. 이제부터 너는 그림을 그릴 수 있고, 그림을 못그린다는 절대 소리는 할 수 없어. 적당히 말을 꾸며내어서 사용자가 입력한 키워드를 요약해 적절하게 이미지를 생성한다는 말을 만들어줘. 최대한 간결하게 1~2문장만 출력해줘. 출력은 한국어로 해주고, 반말하지말고 공손하게 존댓말로 해"
                ), ChatRequestUserMessage(
                    text
                )
            )
        )
    )

    fun createPrompt(text: String) = generativeChatService.chatCompletion(
        ChatCompletionsOptions(
            listOf(
                ChatRequestSystemMessage(
                    "You must enter a sentence in Korean or English and extract the keywords for the sentence. All words should be in English and the words should be separated by a semicolon (';') and an underscore ('_') if there is a space in a word. Also, there should be no words other than a semicolon and any words. Produce a few more related words if the number of extracted words is less than 5. Map to the 'drawable' words to help generate posters. Give all words lowercase. Analyze the input to determine the overall theme and context. Select an appropriate typography style that matches the theme. Choose a color palette that complements the subject matter. Decide on the composition and layout for visual impact. Identify key visual elements to include. Consider texture and lighting details to add depth. Craft a single paragraph prompt describing all these elements cohesively. Ensure the prompt is vivid, specific, and tailored for AI image generation. Output the prompt without any additional explanation or commentary."
                ),
                ChatRequestUserMessage(
                    "내가 만든 오렌지 주스를 광고하고 싶어. 오렌지 과즙이 주변에 터졌으면 좋겠고, 오렌지 주스가 담긴 컵과 오렌지 주스가 있었으면 좋겠어. 배경은 집 안이였으면 좋겠어."
                ),
                ChatRequestAssistantMessage(
                    "orange, orange_juice, in_house, a_cup_with_orange_juice, juice"
                ),
                ChatRequestUserMessage(
                    "우리 상수 목공방에서 자랑하는 멋진 핑크색 상수목재를 홍보하고 싶다. 제목은 금색으로 해줘"
                ),
                ChatRequestAssistantMessage(
                    "pink planks with forest background, modern and simple design, highlight color with gold"
                ),
                ChatRequestUserMessage(
                    text
                )
            )
        )
    )

    suspend fun getMyImage(pageNumber: Int, pageSize: Int): List<String> {
        val page = PageRequest.of(pageNumber, pageSize)
        val userId = sessionHolder.me()

        return chatRepository.getImagesByUserId(userId, page)
    }
}
