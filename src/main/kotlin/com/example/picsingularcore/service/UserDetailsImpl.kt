package com.example.picsingularcore.service

import com.example.picsingularcore.pojo.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(val user: User): UserDetails  {
    // get user roles from database
    override fun getAuthorities(): MutableList<SimpleGrantedAuthority>? {
        return user.roles?.map { role ->
            SimpleGrantedAuthority(role?.name)
        }?.toMutableList()
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

}