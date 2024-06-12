package com.teamapi.palette.util

import com.teamapi.palette.entity.User
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun Mono<Long>.findUser(userRepository: R2dbcRepository<User, Long>) =
    flatMap { userRepository.findById(it) }
        .switchIfEmpty { Mono.error(CustomException(ErrorCode.USER_NOT_FOUND)) }
