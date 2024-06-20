package com.teamapi.palette.service

import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.entity.Chat
import com.teamapi.palette.repository.ChatRepository
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class ChatService (
    private val chatRepository: ChatRepository,
    private val sessionHolder: SessionHolder,
    private val userRepository: UserRepository
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
}