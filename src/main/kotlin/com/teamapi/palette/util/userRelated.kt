package com.teamapi.palette.util

import com.teamapi.palette.entity.Room
import com.teamapi.palette.entity.User
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.service.SuspendSessionHolder
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun Mono<Long>.findUser(userRepository: R2dbcRepository<User, Long>) =
    flatMap { userRepository.findById(it) }
        .switchIfEmpty { Mono.error(CustomException(ErrorCode.USER_NOT_FOUND)) }


suspend fun Room.validateUser(sessionHolder: SuspendSessionHolder): Room {
    val me = sessionHolder.me()
    if (userId != me)
        throw CustomException(ErrorCode.NOT_YOUR_ROOM)
    return this
}
