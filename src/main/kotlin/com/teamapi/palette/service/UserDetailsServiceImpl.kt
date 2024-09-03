package com.teamapi.palette.service

import com.teamapi.palette.repository.UserRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserDetailsServiceImpl(private val userRepository: UserRepository) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByEmail(username).map {
            User.builder().username("${it.id}").password(it.password).roles(it.state.name).build()
        }
    }
}
