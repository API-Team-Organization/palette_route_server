package com.teamapi.palette.ws.actor

import com.teamapi.palette.dto.response.chat.ChatResponse
import com.teamapi.palette.repository.room.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.ws.dto.res.NewChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
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

                        delegateActor.send(DelegateMessage.SendMessage(NewChatMessage.fromDto(msg.chat)))
                    }
                }
            }
        }
}

sealed interface RoomMessages {
    data class NewChat(val roomId: Long, val chat: ChatResponse) : RoomMessages
}
