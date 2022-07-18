package com.example.picsingularcore.controller

import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @Autowired
    lateinit var userRepository: UserRepository

    @PostMapping("/user/login")
    fun login(@RequestBody user: User) : User? {
        val userFound: User? = userRepository.findByUsernameAndPassword(user.username, user.password)
        return userFound
    }

    // register api
    @PostMapping("/user/register")
    fun register(@RequestBody user: User) : String {
        val userFound: User? = userRepository.findByUsernameAndPassword(user.username,user.password)
        return if (userFound != null) {
            "User already exists"
        } else {
            userRepository.save(user)
            "User registered successfully"
        }
    }

}