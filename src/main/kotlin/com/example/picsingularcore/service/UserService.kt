package com.example.picsingularcore.service

import com.example.picsingularcore.common.constant.RolesConstant
import com.example.picsingularcore.dao.RoleRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.Role
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.dto.UserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    fun save(userDTO: UserDTO,roleName: String = RolesConstant.USER.name): User {
        // encode password when save user
        val user = User(username = userDTO.username, password = BCryptPasswordEncoder().encode(userDTO.password))
        // if role is not exist, create new role
        var role = roleRepository.findByName(roleName)
        if (role == null) role = Role(name = roleName)
        user.roles = mutableListOf(role)
        return userRepository.save(user)
    }

}