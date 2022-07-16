package com.example.picsingularcore.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @ResponseBody
    @PostMapping("/user/login")
    fun login() : String {
        println("login")
        return "login"
    }

}