package com.teamapi.palette.entity

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthUserInfo(
    val id: Long, private val email: String, private val password: String
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableSetOf()

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
