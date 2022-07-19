package com.example.picsingularcore.controller

import com.example.picsingularcore.common.utils.JwtUtil
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.UserDTO
import com.example.picsingularcore.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @PostMapping("/user/login")
    fun login(@RequestBody user: UserDTO) : User? {
        // find if token is already in redis and if token is valid, decode it and return user
        val token = redisTemplate.opsForValue().get(user.username) as String?
        if (token != null && jwtUtil.isTokenValid(token) && BCryptPasswordEncoder().matches(user.password, jwtUtil.getPasswordFromToken(token))) {
            return jwtUtil.getClaimFromToken(token,"user") as User
        }
        // if token is not valid or do not in redis, find user in database and check if password is correct
        val userFound: User = userRepository.findByUsername(user.username) ?: throw Exception("User not found")
        if (!BCryptPasswordEncoder().matches(user.password, userFound.password)) throw Exception("Password incorrect")

        // if user is found and password is correct, generate token and save it in redis
        val map = mapOf("user" to userFound)
        val tokenGenerated = jwtUtil.generateToken(map, userFound.username)
        redisTemplate.opsForValue().set(userFound.username, tokenGenerated)
        return userFound
    }

    // register api
    @PostMapping("/user/register")
    fun register(@RequestBody user: UserDTO) : String {
        val userFound: User? = userRepository.findByUsernameAndPassword(user.username,user.password)
        return if (userFound == null) {
            userService.save(user)
            "User registered successfully"
        } else {
            throw Exception("User already exists")
        }
    }
}