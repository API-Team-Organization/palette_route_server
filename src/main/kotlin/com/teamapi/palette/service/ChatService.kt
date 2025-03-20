package com.teamapi.palette.service

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
import com.teamapi.palette.service.adapter.ChatEmitAdapter
import com.teamapi.palette.service.adapter.GenerativeChatAdapter
import com.teamapi.palette.service.infra.GenerativeImageService
import com.teamapi.palette.util.ExceptionReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Instant
import org.bson.types.ObjectId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val qnaRepository: QnARepository,
    private val chatEmitAdapter: ChatEmitAdapter,
    private val sessionHolder: SessionHolder,
    private val roomRepository: RoomRepository,
    private val generativeChatAdapter: GenerativeChatAdapter,
    private val generativeImageService: GenerativeImageService,
    private val exceptionReporter: ExceptionReporter

) {
    private fun getPendingQuestion(qnA: QnA): PromptData? {
        return qnA.qna.find { it.answer == null }
    }

    suspend fun <T : ChatAnswer> createChat(roomId: String, message: T) {
        val userId = sessionHolder.me()
        val room = roomRepository.findByIdOrNull(ObjectId(roomId)) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        var toBeResolved = qnaRepository.getQnAByRoomId(room.id)!!
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

                chatEmitAdapter.emitChat(
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


                chatEmitAdapter.emitChat(
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

                chatEmitAdapter.emitChat(
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
                val userReturn = generativeChatAdapter.roomExecutiveQuestion(additionalInfo = listOf("gpt|data_collect_process_finish", "gpt|generate_queue_added"))
                chatEmitAdapter.emitChat(
                    Chat(
                        resource = ChatState.CHAT,
                        roomId = room.id,
                        userId = userId,
                        isAi = true,
                        message = userReturn
                    )
                )
                generativeImageService.generateImage(toBeResolved, room, userId)
            } else {
                val addResponse = pendingQnAs.first()
                val generated = generativeChatAdapter.roomExecutiveQuestion(addResponse)

                chatEmitAdapter.emitChat(
                    Chat(
                        resource = ChatState.PROMPT,
                        roomId = room.id,
                        userId = userId,
                        isAi = true,
                        message = generated,
                        promptId = addResponse.id
                    )
                )
            }
        }.invokeOnCompletion {
            it?.let {
                exceptionReporter.doReport(it)
            }
        }
    }

    suspend fun getChatList(roomId: String, lastId: String, size: Long): List<ChatResponse> {
        val room = roomRepository.findByIdOrNull(ObjectId(roomId)) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        return chatRepository.getMessageByRoomId(
            room.id,
            Instant.parse(lastId),
            size
        )
    }

    suspend fun getMyImage(pageNumber: Int, pageSize: Int): List<String> {
        val page = PageRequest.of(pageNumber, pageSize)
        val userId = sessionHolder.me()

        return chatRepository.getImagesByUserId(userId, page)
    }
}
