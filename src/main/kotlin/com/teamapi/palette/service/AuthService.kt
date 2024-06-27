package com.teamapi.palette.service

import com.teamapi.palette.dto.auth.LoginRequest
import com.teamapi.palette.dto.auth.RegisterRequest
import com.teamapi.palette.dto.user.PasswordUpdateRequest
import com.teamapi.palette.repository.UserRepository
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

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionHolder: SessionHolder,
    private val authManager: ReactiveAuthenticationManager,
) {
    fun register(request: RegisterRequest): Mono<Void> {
        return userRepository.existsByEmail(request.email)
            .flatMap {
                if (it) Mono.error(CustomException(ErrorCode.USER_ALREADY_EXISTS))
                else userRepository.save(request.toEntity(passwordEncoder)).then()
            }
    }

    fun login(
        request: LoginRequest,
    ): Mono<Void> {
        return authManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
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
            .flatMap { userRepository.delete(it) }
            .then(Mono.defer { webSession.invalidate() })
    }
}
