package com.teamapi.palette.filter

import com.teamapi.palette.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val m = userRepository.findById(username.toLong())
            .block() ?: throw UsernameNotFoundException(username)

        return UserDetailsImpl(m)
    }
}