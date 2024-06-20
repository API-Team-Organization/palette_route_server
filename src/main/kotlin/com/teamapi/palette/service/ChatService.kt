package com.teamapi.palette.service

import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.entity.Chat
import com.teamapi.palette.repository.ChatRepository
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.*
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class ChatService (
    private val chatRepository: ChatRepository,
    private val sessionHolder: SessionHolder,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) {
    fun createChat(request: CreateChatRequest): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap { chatRepository.save(Chat(
                message = request.message,
                datetime = LocalDateTime.now(),
                roomId = request.roomId,
                userId = it.id!!,
                isAi = false
            )) }
            // 인공지능 서버랑 연결하는 로직 필요함
            .then()
    }

    fun getChatList(roomId: Long): Mono<List<ChatResponse>> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap { user ->
                roomRepository.findById(roomId)
                    .switchIfEmpty {
                        error(CustomException(ErrorCode.ROOM_NOT_FOUND))
                    }
                    .flatMapMany { room ->
                        if (room.userId != user.id) {
                            return@flatMapMany Flux.error(CustomException(ErrorCode.FORBIDDEN))
                        }

                        chatRepository.findByRoomId(roomId)
                    }
                    .map {
                        ChatResponse(it.id!!, it.message, it.datetime.atZone(ZoneId.systemDefault()).toInstant(), it.roomId, it.userId, it.isAi)
                    }
                    .collectList()
            }
    }
}