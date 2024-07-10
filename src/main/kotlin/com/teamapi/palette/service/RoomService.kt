package com.teamapi.palette.service

import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.validateUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val sessionHolder: SessionHolder
) {
    fun createRoom(): Mono<RoomResponse> {
        return sessionHolder.me()
            .flatMap { roomRepository.save(Room(userId = it)) }
            .map { RoomResponse(it.id!!, it.title ?: "") }
    }

    fun getRoomList(): Mono<List<RoomResponse>> {
        return sessionHolder.me()
            .flatMapMany { roomRepository.findByUserId(it) }
            .map { RoomResponse(it.id!!, it.title ?: "") }
            .collectList()
    }

    fun updateRoomTitle(updateRoomTitleRequest: UpdateRoomTitleRequest): Mono<Void> {
        return roomRepository
            .findById(updateRoomTitleRequest.id)
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND)))
            .validateUser(sessionHolder)
            .flatMap { roomRepository.save(it.copy(title = updateRoomTitleRequest.title)) }
            .then()
    }

    fun deleteRoom(roomId: Long): Mono<Void> {
        return roomRepository.findById(roomId)
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND)))
            .validateUser(sessionHolder)
            .flatMap { roomRepository.delete(it) }
    }
}
