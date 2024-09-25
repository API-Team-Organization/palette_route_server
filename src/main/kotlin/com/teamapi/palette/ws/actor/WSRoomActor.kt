package com.teamapi.palette.ws.actor

import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.repository.room.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.RoomAction
import com.teamapi.palette.ws.dto.res.ChatMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.springframework.stereotype.Component

@Component
class WSRoomActor(
    private val roomRepository: RoomRepository,
) {
    @ObsoleteCoroutinesApi
    operator fun invoke(roomId: Long, delegateActor: SendChannel<DelegateMessage>) =
        CoroutineScope(Dispatchers.Unconfined).actor<RoomMessages> {
            val roomHooked = roomRepository.findById(roomId)
                ?: return@actor delegateActor.trySend(DelegateMessage.DisconnectWithError(ErrorCode.ROOM_NOT_FOUND))
                    .let {}

            delegateActor.trySend(DelegateMessage.Validate(roomHooked.userId))

            for (msg in channel) {
                when (msg) {
                    is RoomMessages.NewChat -> {
                        if (msg.roomId != roomHooked.id) continue // ignore

                        delegateActor.send(
                            DelegateMessage.SendMessage(ChatMessage.of(msg.action, msg.chat?.toDto()))
                        )
                    }
                }
            }
        }
}

sealed interface RoomMessages {
    data class NewChat(val roomId: Long, val action: RoomAction, val chat: Chat?) : RoomMessages
}
