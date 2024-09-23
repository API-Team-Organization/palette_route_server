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
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val coroutineUserRepository: UserRepository,
    private val userDetailsService: ReactiveUserDetailsService,
    private val verifyCodeRepository: VerifyCodeRepository,
    private val mailVerifyProvider: MailVerifyProvider,
    private val passwordEncoder: PasswordEncoder,
    private val sessionHolder: SessionHolder,
    private val authManager: ReactiveAuthenticationManager,
) {
    suspend fun register(request: RegisterRequest) {
        if (coroutineUserRepository.existsByEmail(request.email))
            throw CustomException(ErrorCode.USER_ALREADY_EXISTS)

        val user = coroutineUserRepository.save(request.toEntity(passwordEncoder))

        createVerifyCode(user) // send verify code

        val auth = createPreAuthorizedToken(user.email)
        return updateSessionWithAuthenticate(auth)

    }

    private suspend fun createVerifyCode(user: User) {
        val verifyCode = mailVerifyProvider.createVerifyCode()
        mailVerifyProvider.sendEmail(user.email, verifyCode)
        verifyCodeRepository.create(VerifyCode(user.id!!, verifyCode))
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
        val info = sessionHolder.me(coroutineUserRepository)
        authManager.authenticate(UsernamePasswordAuthenticationToken(info.email, request.beforePassword))
            .awaitSingleOrNull() ?: throw CustomException(ErrorCode.INVALID_CREDENTIALS)

        coroutineUserRepository.save(
            sessionHolder.me(coroutineUserRepository)
                .copy(password = passwordEncoder.encode(request.afterPassword))
        )

        sessionHolder.getWebSession().invalidate().awaitSingleOrNull()
    }

    suspend fun resign() {
        val user = sessionHolder.me(coroutineUserRepository)
        verifyCodeRepository.deleteById(user.id!!)

        coroutineUserRepository.save(user.copy(state = UserState.DELETED))
        sessionHolder.getWebSession().invalidate().awaitSingle()
    }

    private suspend fun createPreAuthorizedToken(email: String): Authentication {
        return userDetailsService.findByUsername(email).awaitSingle()
            .let { UsernamePasswordAuthenticationToken(it, null, it.authorities) }
    }

    suspend fun verifyEmail(code: String) {
        val me = sessionHolder.me()
        val item = verifyCodeRepository.findById(me)
            ?: throw CustomException(ErrorCode.ALREADY_VERIFIED)

        if (item.code != code) throw CustomException(ErrorCode.INVALID_VERIFY_CODE)

        val saved = coroutineUserRepository.save(
            coroutineUserRepository.findById(item.userId)!!
                .copy(state = UserState.ACTIVE)
        ) // update user

        val newAuthentication = createPreAuthorizedToken(saved.email)
        updateSessionWithAuthenticate(newAuthentication)

        return verifyCodeRepository.delete(item)
    }

    suspend fun resendVerifyCode() {
        val me = sessionHolder.me(coroutineUserRepository)

        if (me.state == UserState.ACTIVE)
            throw CustomException(ErrorCode.ALREADY_VERIFIED)

        verifyCodeRepository.deleteById(me.id!!)
        createVerifyCode(me)
    }
}
