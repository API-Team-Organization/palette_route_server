package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.dto.user.PasswordUpdateRequest
import com.teamapi.palette.entity.User
import com.teamapi.palette.entity.VerifyCode
import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.extern.MailVerifyProvider
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.repository.VerifyCodeRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.findUser
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
import org.springframework.stereotype.Service
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.jvm.optionals.getOrNull

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val verifyCodeRepository: VerifyCodeRepository,
    private val mailVerifyProvider: MailVerifyProvider,
    private val passwordEncoder: PasswordEncoder,
    private val sessionHolder: SessionHolder,
    private val authManager: ReactiveAuthenticationManager,
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.existsByEmail(request.email)
            .flatMap {
                if (it) Mono.error(CustomException(ErrorCode.USER_ALREADY_EXISTS))
                else userRepository.save(request.toEntity(passwordEncoder))
            }
            .flatMap {
                createVerifyCode(it)
                    .thenReturn(it)
            }
            .flatMap {
                authenticate(it.email, request.password)
            }
    }

    private fun createVerifyCode(user: User): Mono<Void> {
        val verifyCode = mailVerifyProvider.createVerifyCode()
        return mailVerifyProvider.sendEmail(user.email, verifyCode)
            .publishOn(Schedulers.boundedElastic())
            .doOnSuccess {
                verifyCodeRepository.save(VerifyCode(user.id!!, verifyCode))
            }
            .then()
    }

    fun authenticate(
        email: String, password: String,
    ): Mono<Void> {
        return authManager.authenticate(
            UsernamePasswordAuthenticationToken(email, password)
        )
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.INVALID_CREDENTIALS)))
            .flatMap { auth ->
                sessionHolder.getSecurityContext().map { it.apply { authentication = auth } }
            } // context
            .flatMap {
                sessionHolder.getWebSession().flatMap { session ->
                    session.attributes[DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME] = it
                    session.changeSessionId()
                }
            }
    }

    fun passwordUpdate(request: PasswordUpdateRequest): Mono<Void> {
        return sessionHolder.userInfo()
            .flatMap {
                authManager.authenticate(UsernamePasswordAuthenticationToken(it.username, request.beforePassword))
            }
            .then(sessionHolder.me())
            .findUser(userRepository)
            .flatMap {
                userRepository.save(it.copy(password = passwordEncoder.encode(request.afterPassword)))
            }
            .then(sessionHolder.getWebSession())
            .flatMap { it.invalidate() }
            .then()
    }

    fun resign(webSession: WebSession): Mono<Void> {
        return sessionHolder
            .me()
            .findUser(userRepository)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { verifyCodeRepository.deleteById(it.id!!) }
            .flatMap { userRepository.delete(it) }
            .then(Mono.defer { webSession.invalidate() })
    }

    fun verifyEmail(code: String): Mono<Void> {
        return sessionHolder.me()
            .publishOn(Schedulers.boundedElastic())
            .map { verifyCodeRepository.findById(it) } // fetch
            .flatMap { Mono.justOrEmpty(it.getOrNull()) } // null check logic

            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ALREADY_VERIFIED)))
            .filter { it.code == code }
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.INVALID_VERIFY_CODE)))

            .flatMap { item ->
                userRepository.findById(item.userId)
                    .map { it.copy(state = UserState.ACTIVE) }
                    .flatMap { userRepository.save(it) }
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext { verifyCodeRepository.delete(item) }
                    .then()
            }
    }

    fun resendVerifyCode(): Mono<Void> {
        return sessionHolder.me()
            .findUser(userRepository)
            .filter { it.state == UserState.CREATED }
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ALREADY_VERIFIED)))

            .publishOn(Schedulers.boundedElastic())
            .doOnNext { verifyCodeRepository.deleteById(it.id!!) } // if exists

            .flatMap { createVerifyCode(it) }
            .then()
    }
}
