package com.example.picsingularcore.service

import com.example.picsingularcore.dao.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl: UserDetailsService {
    @Autowired
    lateinit var userRepository: UserRepository

    override fun loadUserByUsername(username: String?): UserDetails {
        val user = userRepository.findByUsername(username)
        if (user != null) {
            return UserDetailsImpl(user)
        }
        throw IllegalArgumentException("User not found")
    }
}