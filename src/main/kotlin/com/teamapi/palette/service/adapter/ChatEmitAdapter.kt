package com.teamapi.palette.service.adapter

import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.repository.chat.ChatRepository
import com.teamapi.palette.ws.actor.SinkActor
import org.springframework.stereotype.Service

@Service
class ChatEmitAdapter(
    private val chatRepository: ChatRepository,
    private val actor: SinkActor,
) {
    suspend fun emitChat(chat: Chat): Chat {
        actor.addChat(chat.roomId, chat)
        return chatRepository.create(chat)
    }
}
