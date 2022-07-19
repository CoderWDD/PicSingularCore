package com.example.picsingularcore.service

import com.example.picsingularcore.common.constant.RolesConstant
import com.example.picsingularcore.dao.RoleRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.Role
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.UserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    fun save(userDTO: UserDTO): User {
        // encode password when save user
        val user = User(username = userDTO.username, password = BCryptPasswordEncoder().encode(userDTO.password))
        var role = roleRepository.findByName(RolesConstant.USER.name)
        // if role is null, create new role
        if (role == null) role = Role(name = RolesConstant.USER.name)
        user.roles = listOf(role)
        return userRepository.save(user)
    }

    fun login(username: String, password: String): User? {
        return userRepository.findByUsername(username)
    }
}