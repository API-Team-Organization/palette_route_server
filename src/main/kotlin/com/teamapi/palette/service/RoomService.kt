package com.teamapi.palette.service

import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.room.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val sessionHolder: SessionHolder
) {
    suspend fun createRoom(): RoomResponse {
        val room = roomRepository.save(Room(userId = sessionHolder.me()))
        return RoomResponse(room.id!!, room.title, null)
    }

    suspend fun getRoomList(): List<RoomResponse> {
        return roomRepository.findRoomByUserId(sessionHolder.me())
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
