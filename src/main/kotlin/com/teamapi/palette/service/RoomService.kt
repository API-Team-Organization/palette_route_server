package com.teamapi.palette.service

import com.teamapi.palette.dto.response.RoomResponses.*
import com.teamapi.palette.entity.Room
import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.entity.qna.ChatQuestion
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
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val chatRepository: ChatRepository,
    private val qnaRepository: QnARepository,
    private val chatEmitService: ChatEmitService,
    private val sessionHolder: SessionHolder,
    private val generativeChatService: GenerativeChatService,
) {
    private val log = LoggerFactory.getLogger(RoomService::class.java)

    suspend fun createRoom(): RoomResponse {
        val me = sessionHolder.me()
        val room = roomRepository.save(Room(userId = me))

        CoroutineScope(Dispatchers.Unconfined).async {
            val createdQnA = qnaRepository.create(
                QnA(
                    roomId = room.id!!,
                    qna = listOf(
                        PromptData.Selectable(
                            "aspect_ratio",
                            ChatQuestion.SelectableQuestion(
                                listOf(
                                    PromptData.Selectable.Choice("DISPLAY", "16:9"),
                                    PromptData.Selectable.Choice("PAPER", "1:1.41"),
                                    PromptData.Selectable.Choice("SQUARE", "1:1"),
                                    PromptData.Selectable.Choice("TABLET", "4:3")
                                )
                            )
                        ),
                        PromptData.UserInput("product_explanation"),
                        PromptData.UserInput("title"),
                        PromptData.Grid("title_position", ChatQuestion.GridQuestion(3, 3))
                    )
                )
            )

            val completion = generativeChatService.roomWelcomeMessage()

            chatEmitService.emitChat(
                Chat(
                    datetime = Clock.System.now(),
                    resource = ChatState.PROMPT,
                    roomId = room.id,
                    userId = me,
                    isAi = true,
                    message = completion.choices.random().message.content, // TODO
                    promptId = createdQnA.qna.find { it.promptName == "aspect_ratio" }!!.id
                )
            )
        }.invokeOnCompletion {
            if (it != null) {
                log.error("Error while generating chat", it)
            }
        }

        return RoomResponse(room.id!!, room.title, null)
    }

    suspend fun getQnA(roomId: Long): List<QnAResponse> {
        return qnaRepository.getQnAByRoomId(roomId)!!.qna.map {
            QnAResponse(
                it.id.toString(),
                it.type,
                it.question,
                it.answer,
                it.promptName
            )
        }
    }

    suspend fun getRoomList(): List<RoomResponse> {
        val me = sessionHolder.me()
        val rooms = roomRepository.findRoomByUserId(me).toList()

        val messageSearched = chatRepository.getLatestMessageMapById(rooms.map { it.id!! })
        return rooms.map { RoomResponse(it.id!!, it.title, messageSearched[it.id]) }
    }

    suspend fun updateRoomTitle(roomId: Long, title: String) {
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)
        roomRepository.save(room.copy(title = title))
    }

    suspend fun deleteRoom(roomId: Long) {
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        return roomRepository.delete(room)
    }
}
