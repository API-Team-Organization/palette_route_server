package com.teamapi.palette.service

import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RoomService (
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val sessionHolder: SessionHolder
) {
    fun createRoom(): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .flatMap { roomRepository.save(Room(user = it)).then() }
    }
}
