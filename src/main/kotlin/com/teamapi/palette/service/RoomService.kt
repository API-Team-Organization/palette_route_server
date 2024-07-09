package com.teamapi.palette.service

import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class RoomService (
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    fun createRoom(): Mono<RoomResponse> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap {
                roomRepository.save(Room(userId = it.id!!))
            }
            .map {
                RoomResponse(it.id!!, it.title ?: "")
            }
    }

    fun getRoomList(): Mono<List<RoomResponse>> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMapMany {
                roomRepository.findByUserId(it.id!!)
            }
            .map {
                RoomResponse(it.id!!, it.title ?: "")
            }
            .collectList()
    }

    fun updateRoomTitle(updateRoomTitleRequest: UpdateRoomTitleRequest): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap { user ->
                roomRepository
                    .findById(updateRoomTitleRequest.id)
                    .switchIfEmpty { Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND)) }
                    .flatMap { room ->
                        if (user.id == room.userId) {
                            roomRepository
                                .save(room.copy(title = updateRoomTitleRequest.title))
                        } else {
                            Mono.error(CustomException(ErrorCode.FORBIDDEN))
                        }
                    }
            }
            .then()
    }

    fun deleteRoom(roomId: Long): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap { user ->
                roomRepository.findById(roomId)
                    .switchIfEmpty {
                        Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND))
                    }
                    .flatMap { room ->
                        if (room.userId == user.id)
                            roomRepository.delete(room)
                        else
                            Mono.error(CustomException(ErrorCode.FORBIDDEN))
                    }
            }
    }
}
