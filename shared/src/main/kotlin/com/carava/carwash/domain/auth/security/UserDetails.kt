package com.carava.carwash.domain.auth.security

import com.carava.carwash.common.constants.UserType
import com.carava.carwash.domain.auth.entity.Auth
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetails(
    private val auth: Auth,
    private val userType: UserType,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${userType.name}"))
    }

    override fun getPassword() = auth.password

    override fun getUsername() = auth.email

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

}