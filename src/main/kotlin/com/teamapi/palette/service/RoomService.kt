package com.teamapi.palette.service

import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val sessionHolder: SessionHolder
) {
    suspend fun createRoom(): RoomResponse {
        val room = roomRepository.save(Room(userId = sessionHolder.me()))
        return RoomResponse(room.id!!, room.title ?: "")
    }

    suspend fun getRoomList(): List<RoomResponse> {
        return roomRepository.findByUserId(sessionHolder.me())
            .map { RoomResponse(it.id!!, it.title ?: "") }
            .toList()
    }

    suspend fun updateRoomTitle(updateRoomTitleRequest: UpdateRoomTitleRequest) {
        val room = roomRepository.findById(updateRoomTitleRequest.id) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)
        roomRepository.save(room.copy(title = updateRoomTitleRequest.title))
    }

    suspend fun deleteRoom(roomId: Long) {
        val room = roomRepository.findById(roomId) ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)
        room.validateUser(sessionHolder)

        return roomRepository.delete(room)
    }
}
