package com.teamapi.palette.service

import com.teamapi.palette.entity.User
import com.teamapi.palette.repository.user.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

@Component
class SessionHolder {
    private suspend fun getServerWebExchange(): ServerWebExchange {
        return Mono.deferContextual {
            Mono.just(it.get(ServerWebExchange::class.java))
        }.awaitSingleOrNull() ?: throw CustomException(ErrorCode.INVALID_SESSION)
    }

    suspend fun getWebSession(): WebSession {
        return getServerWebExchange().session.awaitSingle()
    }

    suspend fun getSecurityContext(sessionOrNull: WebSession? = null): SecurityContext {
        val session = sessionOrNull ?: getWebSession()
        val context = ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()
            ?: SecurityContextImpl().also {
                session.attributes[DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME] = it
            }
        return context
    }

    suspend fun userInfo(): UserDetails {
        return getSecurityContext().authentication.principal as UserDetails
    }

    suspend fun me(): ObjectId {
        return ObjectId(userInfo().username)
    }

    suspend fun me(repository: UserRepository): User {
        return repository.findByIdOrNull(me())
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }
}
