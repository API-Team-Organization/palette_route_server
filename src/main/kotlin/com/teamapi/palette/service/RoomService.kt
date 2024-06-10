package com.teamapi.palette.service

import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class RoomService (
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    fun createRoom(): Mono<Void> {
        return sessionHolder
            .me()
            .flatMap { userRepository.findById(it) }
            .switchIfEmpty { Mono.error(CustomException(ErrorCode.USER_NOT_FOUND)) }
            .flatMap { roomRepository.save(Room(user = it)).then() }
    }
}