package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.RolesConstant
import com.example.picsingularcore.common.constant.SuperAdminConstants
import com.example.picsingularcore.common.utils.JwtUtil
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.Role
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.dto.UserDTO
import com.example.picsingularcore.service.UserService
import org.apache.logging.log4j.message.StringFormattedMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
class SuperAdminController {
    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var httpServletResponse: HttpServletResponse

    @PostMapping("/superadmin/register/{code}")
    fun register(@RequestBody user: UserDTO,@PathVariable(name = "code") code: String): String {
        // if code is not correct, return Invalid code
        val superAdminCode = redisTemplate.opsForValue().get(SuperAdminConstants.SUPER_ADMIN_CODE)
        if (superAdminCode != code) throw Exception("Invalid code")
        userService.save(user, RolesConstant.SUPER_ADMIN.name)
        return "Super Admin registered successfully"
    }

    @PostMapping("/superadmin/login")
    fun login(@RequestBody user: UserDTO): User? {
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token) && BCryptPasswordEncoder().matches(user.password, jwtUtil.getPasswordFromToken(token))) {
            // add token to response header
            httpServletResponse.addHeader("Authorization", "Bearer $token")
            return jwtUtil.getUserFromToken(token)
        }
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("Super Admin not found")
        if (!BCryptPasswordEncoder().matches(user.password, userFound.password)) throw Exception("Password incorrect")
        // if user is found and password is correct, generate token, add it to response header and add it to redis
        val map = mutableMapOf<String, Any>("user" to userFound)
        val tokenGenerated = jwtUtil.generateToken(map, userFound.username)
        redisTemplate.opsForValue().set(user.username, tokenGenerated)
        httpServletResponse.addHeader("Authorization", "Bearer $token")
        return userFound
    }

    @GetMapping("/superadmin/logout")
    fun logout(authentication: Authentication): String {
        val user = userRepository.findByUsername(authentication.name)!!
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token)) {
            redisTemplate.delete(user.username)
            return "Logout successfully"
        }
        throw Exception("Super Admin not logged in")
    }

    @PostMapping("/superadmin/delete")
    fun delete(authentication: Authentication): String {
        val user = userRepository.findByUsername(authentication.name)!!
        redisTemplate.delete(user.username)
        userRepository.deleteById(user.userId!!)
        return "Delete successfully"
    }

    // update super admin password
    @PostMapping("/superadmin/update")
    fun update(authentication: Authentication,@RequestBody user: UserDTO): String {
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("Super Admin not found")
        if (userFound.username != authentication.name) throw Exception("Username incorrect")
        userService.save(user, RolesConstant.SUPER_ADMIN.name)
        return "Super admin updated successfully"
    }

    @GetMapping("/superadmin/admin/all/{page}/{size}")
    fun getAdmins(
            @PathVariable(name = "page") page: Int,
            @PathVariable(name = "size") size: Int
    ): List<User> {
        return userRepository.findAll(
            (Specification{ root, _, cb ->
                cb.isMember(RolesConstant.SUPER_ADMIN.name, root.get<List<Role>>("roles"))
            }),
            PageRequest.of(page - 1, size)
        ).content
    }

    // update admin password
    @PostMapping("/superadmin/admin/update")
    fun updateAdmin(authentication: Authentication, @RequestBody user: UserDTO): String {
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("Admin not found")
        if (userFound.username == authentication.name) throw Exception("You cannot update yourself")
        if (!userFound.roles.contains(Role(name = RolesConstant.ADMIN.name))) throw Exception("You cannot update a non-admin")
        if (userFound.roles.contains(Role(name = RolesConstant.SUPER_ADMIN.name))) throw Exception("You cannot update a super-admin")
        userService.save(user, RolesConstant.SUPER_ADMIN.name)
        return "Admin updated successfully"
    }

    @PostMapping("/superadmin/admin/delete/{username}")
    fun deleteAdmin(authentication: Authentication,@PathVariable(name = "username") username: String): String{
        val userFound = userRepository.findByUsername(username) ?: throw Exception("Admin not found")
        if (userFound.username == authentication.name) throw Exception("You cannot update yourself")
        if (!userFound.roles.contains(Role(name = RolesConstant.ADMIN.name))) throw Exception("You cannot delete a non-admin")
        if (userFound.roles.contains(Role(name = RolesConstant.SUPER_ADMIN.name))) throw Exception("You cannot delete a super-admin")
        userRepository.delete(userFound)
        return "Admin deleted successfully"
    }


}