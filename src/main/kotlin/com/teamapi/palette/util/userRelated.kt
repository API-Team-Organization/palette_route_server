package com.teamapi.palette.util

import com.teamapi.palette.entity.Room
import com.teamapi.palette.entity.User
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.service.SessionHolder
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun Mono<Long>.findUser(userRepository: R2dbcRepository<User, Long>) =
    flatMap { userRepository.findById(it) }
        .switchIfEmpty { Mono.error(CustomException(ErrorCode.USER_NOT_FOUND)) }


fun Mono<Room>.validateUser(sessionHolder: SessionHolder): Mono<Room> =
    zipWith(sessionHolder.me())
        .filter { it.t1.userId == it.t2 }
        .switchIfEmpty(Mono.error(CustomException(ErrorCode.NOT_YOUR_ROOM)))
        .map { it.t1 }
