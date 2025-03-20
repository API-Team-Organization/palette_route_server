package com.teamapi.palette.service

import com.teamapi.palette.repository.user.UserRepository
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserDetailsServiceImpl(private val userRepository: UserRepository) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> {
        return mono {
            userRepository.findByEmail(username)?.let {
                User.builder().username("${it.id}").password(it.password).roles(it.state.name).build()
            }
        }
    }
}
