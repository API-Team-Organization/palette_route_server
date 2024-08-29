package com.teamapi.palette.service

import com.teamapi.palette.entity.AuthUserInfo
import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByEmail(username)
            .map { AuthUserInfo(it.id!!, it.email, it.password, listOf(SimpleGrantedAuthority(it.state.name))) }
    }
}
