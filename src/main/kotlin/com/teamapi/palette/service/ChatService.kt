package com.teamapi.palette.service

import com.azure.ai.openai.models.ChatCompletions
import com.azure.ai.openai.models.ChatCompletionsOptions
import com.azure.ai.openai.models.ChatRequestSystemMessage
import com.azure.ai.openai.models.ChatRequestUserMessage
import com.teamapi.palette.dto.response.ChatResponses.ChatResponse
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val qnaRepository: QnARepository,
    private val chatEmitService: ChatEmitService,
    private val sessionHolder: SessionHolder,
    private val roomRepository: RoomRepository,
    private val generativeChatService: GenerativeChatService,
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    private fun getPendingQuestion(qnA: QnA): PromptData? {
        return qnA.qna.find { it.answer == null }
    }

    suspend fun <T : ChatAnswer> createChat(roomId: Long, message: T) {
        val userId = sessionHolder.me()
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        var toBeResolved = qnaRepository.getQnAByRoomId(room.id!!)!!
        val pendingQuestion = getPendingQuestion(toBeResolved) ?: kotlin.run {
            log.error("no qna list in room '{}'.", room.id)
            throw CustomException(ErrorCode.INTERNAL_SERVER_EXCEPTION)
        }
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
                val maxSize = grid.question.xSize * grid.question.ySize
                val exceeds = message.choice.filter { it >= maxSize }
                if (exceeds.isNotEmpty())
                    throw CustomException(ErrorCode.QNA_INVALID_GRID_CHOICES, exceeds.joinToString(", "), maxSize - 1)

                if (message.choice.size > maxSize)
                    throw CustomException(ErrorCode.QNA_INVALID_GRID_ABOVE_MAX, maxSize)

                if (message.choice.size != message.choice.distinct().size)
                    throw CustomException(ErrorCode.QNA_INVALID_GRID_DUPE, (message.choice.withIndex().let {
                        @Suppress("ConvertArgumentToSet")
                        it - it.distinctBy { it.value } // distinct is same with toSet
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
                // TODO: Handle Image processing
                chatEmitService.emitChat(
                    Chat(
                        roomId = room.id,
                        userId = userId,
                        isAi = true,
                        message = "원하는 질문이 다 채워졌따. 만드느라 수고햇다. 이제 서버에서 이미지 생성 다만들기를 기다려"
                    )
                )
                return@async
            }

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
        }.invokeOnCompletion {
            it?.let {
                log.error("error", it)
            }
        }

        /*
        val userId = sessionHolder.me()

        actor.addChat(roomId, RoomAction.START, myMsg)

        CoroutineScope(Dispatchers.Unconfined).async { // ignore
            try {
                val chat = createUserReturn(message).awaitSingle()
                val textChat = Chat(
                    message = chat.choices[0].message.content,
                    datetime = ZonedDateTime.now(),
                    roomId = roomId,
                    userId = userId,
                    isAi = true
                )
                actor.addChat(roomId, RoomAction.TEXT, textChat)

                val image = generatedImageService.draw(message).awaitSingle()
                val stamp = ZonedDateTime.now()

                chatEmitService.emitChat(
                    RoomAction.IMAGE,
                    Chat(
                        message = image.data[0].url,
                        datetime = stamp,
                        resource = ChatState.IMAGE,
                        roomId = roomId,
                        userId = userId,
                        isAi = true
                    )
                )
                chatRepository.save(textChat.copy(datetime = stamp.plusSeconds(2))) // late save with delayed time
            } catch (e: CustomException) {
                val errorChat = Chat(
                    message = e.responseCode.message.format(*e.formats),
                    datetime = ZonedDateTime.now(),
                    roomId = roomId,
                    userId = userId,
                    isAi = true
                )
//                    runBlocking { chatRepository.save(errorChat) } // TODO: Save?
                actor.addChat(roomId, RoomAction.END, errorChat)
                return@async
            } catch (e: Exception) {
                val errorChat = Chat(
                    message = "포스터를 생성하는 도중 문제가 발생 했어요. 다음에 다시 시도 해 주세요.",
                    datetime = ZonedDateTime.now(),
                    roomId = roomId,
                    userId = userId,
                    isAi = true
                )
//                    runBlocking { chatRepository.save(errorChat) } // TODO: Save?
                actor.addChat(roomId, RoomAction.END, errorChat)
                return@async
            }

            actor.addChat(roomId, RoomAction.END, null)
        }.invokeOnCompletion { e ->
            if (e != null) {
                log.error("Error while creating chat", e)
            }
        }*/
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

    // TODO: Apply Comfy, Prompt enhancing - in next semester
//    fun draw(query: String) =
//        webui.txt2Img(
//        Txt2ImageOptions(
//            samplerName = SamplingMethod.EULER_A,
//            scheduler = Scheduler.SGM_UNIFORM,
//            steps = 10,
//            width = 768,
//            height = 1024,
//            cfgScale = 6.5,
//            prompt = "score_9, score_8, score_7_up, simplified, (($query):0.9), <lora:pytorch_lora_weights:1>, <lora:LineArt Mono Style LoRA_Pony XL v6:1>",
//            negativePrompt = "bad_hands, aidxlv05_neg, (nsfw:1.5), nude, furry, text",
//        )
//    )

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

    //    fun createPrompt(text: String) = chatCompletion(
//        ChatCompletionsOptions(
//            listOf(
//                ChatRequestSystemMessage(
//                    "You must enter a sentence in Korean or English and extract the keywords for the sentence. All words should be in English and the words should be separated by a semicolon (',') and an underscore ('_') if there is a space in a word. Also, there should be no words other than a semicolon and any words. Produce a few more related words if the number of extracted words is less than 5. Map to  the 'drawable' words to help generate posters. Give all words lowercase."
//                ),
//                ChatRequestUserMessage(
//                    "내가 만든 오렌지 주스를 광고하고 싶어. 오렌지 과즙이 주변에 터졌으면 좋겠고, 오렌지 주스가 담긴 컵과 오렌지 주스가 있었으면 좋겠어. 배경은 집 안이였으면 좋겠어."
//                ),
//                ChatRequestAssistantMessage(
//                    "orange, orange_juice, in_house, a_cup_with_orange_juice, juice"
//                ),
//                ChatRequestUserMessage(
//                    text
//                )
//            )
//        )
//    )
    suspend fun getMyImage(pageNumber: Int, pageSize: Int): List<String> {
        val page = PageRequest.of(pageNumber, pageSize)
        val userId = sessionHolder.me()

        return chatRepository.getImagesByUserId(userId, page)
    }
}
