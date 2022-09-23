package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.FilePathConstant
import com.example.picsingularcore.common.utils.JwtUtil
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.dto.UserDTO
import com.example.picsingularcore.pojo.dto.UserInfoDTO
import com.example.picsingularcore.pojo.dto.UserUpdateDTO
import com.example.picsingularcore.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class UserController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    lateinit var httpServletResponse: HttpServletResponse

    @PostMapping("/user/login")
    fun login(@RequestBody user: UserDTO) : User? {
        // find if token is already in redis and if token is valid, decode it and return user
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token) && BCryptPasswordEncoder().matches(user.password, jwtUtil.getPasswordFromToken(token))) {
            // add token to response header
            httpServletResponse.addHeader("Authorization", "Bearer $token")
            return jwtUtil.getUserFromToken(token)
        }
        // if token is not valid or do not in redis, find user in database and check if password is correct
        val userFound: User = userRepository.findByUsername(user.username) ?: throw Exception("User not found")
        if (!BCryptPasswordEncoder().matches(user.password, userFound.password)) throw Exception("Password incorrect")

        // if user is found and password is correct, generate token and save it in redis
        val map = mutableMapOf<String,Any>("user" to userFound)
        val tokenGenerated = jwtUtil.generateToken(map, userFound.username)
        redisTemplate.opsForValue().set(userFound.username, tokenGenerated)
        httpServletResponse.addHeader("Authorization", "Bearer $tokenGenerated")
        return userFound
    }

    // register api
    @PostMapping("/user/register")
    fun register(@RequestBody user: UserDTO) : String {
        val userFound: User? = userRepository.findByUsername(user.username)
        return if (userFound == null) {
            userService.save(user)
            "User registered successfully"
        } else {
            throw Exception("User already exists")
        }
    }

    // logout api
    @PostMapping("/user/logout")
    fun logout(authentication: Authentication) : String {
        val user = userRepository.findByUsername(authentication.name)!!
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token)) {
            redisTemplate.delete(user.username)
            return "User logged out successfully"
        }
        throw Exception("User not logged in")
    }

    // get user api
    @GetMapping("/user/info")
    fun getUser(authentication: Authentication): User {
        return userRepository.findByUsername(authentication.name)!!
    }

    @GetMapping("/user/info/{id}")
    fun getUserById(@PathVariable id: Long): UserInfoDTO {
        val userFound = userRepository.findById(id).get()
        return UserInfoDTO(userId = userFound.userId!!,username = userFound.username, avatar = userFound.avatar,signature = userFound.signature)
    }

    // upload profile avatar
    @PostMapping("/user/avatar")
    fun uploadAvatar(authentication: Authentication, @RequestPart(name = "avatar") multipartFile: MultipartFile) : User {
        if (multipartFile == null) throw Exception("File is Null")
        if (multipartFile.isEmpty) throw Exception("File is empty")
        val filePath = FilePathConstant.IMAGE_PATH + authentication.name + "/avatar/"
        File(filePath).mkdirs()
        val fileName = UUID.randomUUID().toString() + "-" + multipartFile.originalFilename
        val file = File(filePath + fileName)
        // write multipartFile to file
        file.writeBytes(multipartFile.bytes)
        val user = userRepository.findByUsername(authentication.name)!!
        user.avatar = fileName
        return userRepository.save(user)
    }

    // get profile avatar
//    @GetMapping("/images/user/avatar")
//    fun getAvatar(authentication: Authentication) {
//        val user = userRepository.findByUsername(authentication.name)!!
//        val path = FilePathConstant.IMAGE_PATH + authentication.name + "/avatar/" + user.avatar!!
//        val file = File(path)
//        if (!file.exists()){
//            throw Exception("Avatar not found")
//        }
////        httpServletResponse.contentType = "image/jpeg, image/jpg, image/png, image/gif, image/bmp, image/webp, image/svg+xml, image/x-icon, image/vnd.microsoft.icon"
//        file.inputStream().copyTo(httpServletResponse.outputStream)
//    }
//
//    @GetMapping("/images/user/info/avatar/{username}")
//    fun getAvatarByUrl(@PathVariable(value = "username") username: String){
//        val user = userRepository.findByUsername(username) ?: throw Exception("User avatar is null")
//        val path = FilePathConstant.IMAGE_PATH + username + "/avatar/" + user.avatar
//        val file = File(path)
//        if (!file.exists()){
//            throw Exception("Avatar not found")
//        }
//        httpServletResponse.contentType = "image/jpeg, image/jpg, image/png, image/gif, image/bmp, image/webp, image/svg+xml, image/x-icon, image/vnd.microsoft.icon"
//        file.inputStream().copyTo(httpServletResponse.outputStream)
//    }

    @GetMapping("/user/avatar/url")
    fun getAvatarUrl(authentication: Authentication) : String {
        val user = userRepository.findByUsername(authentication.name) ?: throw Exception("User avatar is null")
        return FilePathConstant.IMAGE_PATH + authentication.name + "/avatar/" + user.avatar!!
    }

    // update profile
    @PostMapping("/user/update")
    fun update(authentication: Authentication, @RequestBody user: UserUpdateDTO) : User {
        // if username changed
        if (user.username!!.isNotEmpty() && user.username != authentication.name){
            // if username has been register, then throw exception
            userRepository.findByUsername(user.username)?.let { throw Exception("Username already exists") }
            // if new username is valid, then change user's file path name
            val filePath = FilePathConstant.IMAGE_PATH + authentication.name + "/"
            val file = File(filePath)
            file.renameTo(File(FilePathConstant.IMAGE_PATH + user.username + "/"))
        }
        val userFound = userRepository.findByUsername(authentication.name)!!
        userFound.username = user.username
        userFound.avatar = user.avatar ?: userFound.avatar
        userFound.signature = user.signature ?: userFound.signature
        userFound.password = user.password ?: BCryptPasswordEncoder().encode(userFound.password)
        // refresh token and update it in redis, put token in response header
        val map = mapOf("user" to userFound)
        val tokenGenerated = jwtUtil.generateToken(map, userFound.username)
        redisTemplate.opsForValue().set(userFound.username, tokenGenerated)
        httpServletResponse.addHeader("Authorization", "Bearer $tokenGenerated")
        return userRepository.save(userFound)
    }

}