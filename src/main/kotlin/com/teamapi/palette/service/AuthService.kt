package com.teamapi.palette.service

import com.teamapi.palette.dto.request.auth.RegisterRequest
import com.teamapi.palette.dto.request.user.PasswordUpdateRequest
import com.teamapi.palette.entity.User
import com.teamapi.palette.entity.VerifyCode
import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.service.adapter.MailSendAdapter
import com.teamapi.palette.repository.user.UserRepository
import com.teamapi.palette.repository.VerifyCodeRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userDetailsService: ReactiveUserDetailsService,
    private val verifyCodeRepository: VerifyCodeRepository,
    private val mailSendAdapter: MailSendAdapter,
    private val passwordEncoder: PasswordEncoder,
    private val sessionHolder: SessionHolder,
    private val authManager: ReactiveAuthenticationManager,
) {
    suspend fun register(request: RegisterRequest) {
        if (userRepository.existsByEmail(request.email))
            throw CustomException(ErrorCode.USER_ALREADY_EXISTS)

        val user = userRepository.create(request.toEntity(passwordEncoder))

        createVerifyCode(user) // send verify code

        val auth = createPreAuthorizedToken(user.email)
        return updateSessionWithAuthenticate(auth)
    }

    private suspend fun createVerifyCode(user: User) {
        val verifyCode = mailSendAdapter.createVerifyCode()
        mailSendAdapter.sendEmail(user.email, verifyCode)
        verifyCodeRepository.create(VerifyCode(user.id.toString(), verifyCode))
    }

    suspend fun authenticate(email: String, password: String) {
        val auth =
            authManager.authenticate(UsernamePasswordAuthenticationToken(email, password)).awaitSingleOrNull()
                ?: throw CustomException(ErrorCode.INVALID_CREDENTIALS)

        return updateSessionWithAuthenticate(auth)
    }

    private suspend fun updateSessionWithAuthenticate(auth: Authentication) {
        val session = sessionHolder.getWebSession()
        val context = sessionHolder.getSecurityContext(session)

        context.authentication = auth
        session.attributes[DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME] = context

        session.changeSessionId().awaitSingleOrNull()
    }

    suspend fun passwordUpdate(request: PasswordUpdateRequest) {
        val info = sessionHolder.me(userRepository)
        authManager.authenticate(UsernamePasswordAuthenticationToken(info.email, request.beforePassword))
            .awaitSingleOrNull() ?: throw CustomException(ErrorCode.INVALID_CREDENTIALS)

        userRepository.create(
            sessionHolder.me(userRepository)
                .copy(password = passwordEncoder.encode(request.afterPassword))
        )

        sessionHolder.getWebSession().invalidate().awaitSingleOrNull()
    }

    suspend fun resign() {
        val user = sessionHolder.me(userRepository)
        verifyCodeRepository.deleteById(user.id.toString())

        userRepository.create(user.copy(state = UserState.DELETED))
        sessionHolder.getWebSession().invalidate().awaitSingle()
    }

    private suspend fun createPreAuthorizedToken(email: String): Authentication {
        return userDetailsService.findByUsername(email).awaitSingle()
            .let { UsernamePasswordAuthenticationToken(it, null, it.authorities) }
    }

    suspend fun verifyEmail(code: String) {
        val me = sessionHolder.me()
        val item = verifyCodeRepository.findById(me.toString())
            ?: throw CustomException(ErrorCode.ALREADY_VERIFIED)

        if (item.code != code) throw CustomException(ErrorCode.INVALID_VERIFY_CODE)

        val saved = userRepository.create(
            userRepository.findByIdOrNull(ObjectId(item.userId))!!
                .copy(state = UserState.ACTIVE)
        ) // update user

        val newAuthentication = createPreAuthorizedToken(saved.email)
        updateSessionWithAuthenticate(newAuthentication)

        return verifyCodeRepository.delete(item)
    }

    suspend fun resendVerifyCode() {
        val me = sessionHolder.me(userRepository)

        if (me.state == UserState.ACTIVE)
            throw CustomException(ErrorCode.ALREADY_VERIFIED)

        verifyCodeRepository.deleteById(me.id.toString())
        createVerifyCode(me)
    }
}
