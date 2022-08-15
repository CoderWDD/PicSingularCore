package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.RolesConstant
import com.example.picsingularcore.common.utils.DTOUtil.pagesToPagesDTO
import com.example.picsingularcore.common.utils.JwtUtil
import com.example.picsingularcore.dao.CommentRepository
import com.example.picsingularcore.dao.SecondCommentRepository
import com.example.picsingularcore.dao.SingularRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.*
import com.example.picsingularcore.pojo.dto.PagesDTO
import com.example.picsingularcore.pojo.dto.UserDTO
import com.example.picsingularcore.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
class AdminController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    lateinit var httpServletResponse: HttpServletResponse

    @Autowired
    lateinit var singularRepository: SingularRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var secondCommentRepository: SecondCommentRepository

    @PostMapping("/admin/login")
    fun login(@RequestBody user: UserDTO): User?{
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token) && BCryptPasswordEncoder().matches(user.password, jwtUtil.getPasswordFromToken(token))) {
            // add token to response header
            httpServletResponse.addHeader("Authorization", "Bearer $token")
            return jwtUtil.getUserFromToken(token)
        }
        // if token is not valid or not found in redis, then login
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("User not found")
        if (!BCryptPasswordEncoder().matches(user.password, userFound.password)) throw Exception("Password incorrect")
        // if admin is found, then generate token and add to redis
        val map = mutableMapOf<String, Any>("user" to userFound)
        val generateToken = jwtUtil.generateToken(map, userFound.username)
        redisTemplate.opsForValue().set(user.username, generateToken)
        httpServletResponse.addHeader("Authorization", "Bearer $token")
        return userFound

    }

    @PostMapping("/admin/logout")
    fun logout(authentication: Authentication): String{
        val user = userRepository.findByUsername(authentication.name)!!
        val token = redisTemplate.opsForValue().get(user.username)
        if (token != null && jwtUtil.isTokenValid(token)) {
            redisTemplate.delete(user.username)
            return "Logout successfully"
        }
        throw Exception("Admin not logged in")
    }

    // only super admin can access this endpoint
    @PostMapping("/admin/register")
    fun register(@RequestBody user: UserDTO): String{
        if (userRepository.findByUsername(user.username) != null){
            throw Exception("Admin username already exists")
        }
        userService.save(user,RolesConstant.ADMIN.name)
        return "Admin registered successfully"
    }

    @PostMapping("/admin/delete")
    fun delete(authentication: Authentication): String {
        val user = userRepository.findByUsername(authentication.name)!!
        redisTemplate.delete(user.username)
        userRepository.deleteById(user.userId!!)
        return "Delete successfully"
    }

    @PostMapping("/admin/update")
    fun update(authentication: Authentication,@RequestBody user: UserDTO): String {
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("Admin not found")
        if (userFound.username != authentication.name) throw Exception("Username incorrect")
        userService.save(user, RolesConstant.ADMIN.name)
        return "Admin updated successfully"
    }

    @GetMapping("/admin/user/all/{page}/{size}")
    fun getAllUsers(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<User>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val userPages =  userRepository.findAll(
            (Specification { root, _, cb->
                cb.isMember(RolesConstant.USER.name, root.get<List<Role>>("roles"))
            }),
            PageRequest.of(page - 1, size)
        )
        return pagesToPagesDTO(userPages)
    }

    @PostMapping("/admin/user/delete/{username}")
    fun deleteUser(authentication: Authentication, @PathVariable(name = "username") username: String): String{
        val user = userRepository.findByUsername(username) ?: throw Exception("User not found")
        if (user.username == authentication.name) throw Exception("You cannot delete yourself")
        if (!user.roles.contains(Role(name = RolesConstant.USER.name))) throw Exception("You cannot delete a non-user")
        userRepository.delete(user)
        return "User deleted successfully"
    }

    @PostMapping("/admin/user/update")
    fun updateUser(authentication: Authentication, @RequestBody user: UserDTO): String{
        val userFound = userRepository.findByUsername(user.username) ?: throw Exception("User not found")
        if (userFound.roles.contains(Role(name = RolesConstant.ADMIN.name)) || userFound.roles.contains(Role(name = RolesConstant.SUPER_ADMIN.name))) throw Exception("You can only update a user")
        userService.save(user,RolesConstant.USER.name)
        return "User updated successfully"
    }

    @GetMapping("/admin/singular/list/{username}/{page}/{size}")
    fun getSingularList(
        authentication: Authentication,
        @PathVariable(name = "username") username: String,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val user = userRepository.findByUsername(username) ?: throw Exception("User not found")
        val singularPages =  singularRepository.findAll(
            (Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<User>("user"), user)
            }),
            PageRequest.of(page - 1, size, Sort.by( "pushDate").descending())
        )
        return pagesToPagesDTO(singularPages)
    }

    @PostMapping("/admin/singular/delete/{id}")
    fun deleteSingular(authentication: Authentication, @PathVariable(name = "id") id: Long): String{
        if (!singularRepository.existsById(id)) throw Exception("Singular not found")
        singularRepository.deleteById(id)
        return "Singular deleted successfully"
    }

    @GetMapping("/admin/comment/first/list/{singularId}/{page}/{size}")
    fun getFirstCommentList(
        authentication: Authentication,
        @PathVariable(name = "singularId") singularId: Long,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<CommentLevelFirst>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        if (!singularRepository.existsById(singularId)) throw Exception("Singular not found")
        val commentPages =  commentRepository.findAll(
            (Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<Long>("singularId"), singularId)
            }),
            PageRequest.of(page - 1, size, Sort.by("likeCount").descending().and(Sort.by("createDate").descending()))
        )
        return pagesToPagesDTO(commentPages)
    }

    @GetMapping("/admin/comment/second/list/{singularId}/{firstCommentId}/{page}/{size}")
    fun getSecondCommentList(
        authentication: Authentication,
        @PathVariable(name = "singularId") singularId: Long,
        @PathVariable(name = "firstCommentId") firstCommentId: Long,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<CommentLevelSecond>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        if (!singularRepository.existsById(singularId)) throw Exception("singular not found")
        if (!commentRepository.existsById(firstCommentId)) throw Exception("First comment not found")
        val secondCommentPages =  secondCommentRepository.findAll(
            (Specification { root, _, cb ->
                cb.and(
                    cb.equal(root.get<Long>("singularId"), singularId),
                    cb.equal(root.get<Long>("parentCommentId"), firstCommentId)
                )
            }),
            PageRequest.of(page - 1, size, Sort.by("likeCount").descending().and(Sort.by("createDate").descending()))
        )
        return pagesToPagesDTO(secondCommentPages)
    }

    @PostMapping("/admin/comment/first/delete/{singularId}/{commentId}")
    fun deleteFirstComment(authentication: Authentication, @PathVariable(name = "singularId") singularId: Long, @PathVariable(name = "commentId") commentId: Long): String{
        if (!singularRepository.existsById(singularId)) throw Exception("Singular not found")
        if (!commentRepository.existsById(commentId)) throw Exception("Comment not found")
        commentRepository.deleteById(commentId)
        return "Comment deleted successfully"
    }

    @PostMapping("/admin/comment/second/delete/{singularId}/{firstCommentId}/{commentId}")
    fun deleteSecondComment(authentication: Authentication, @PathVariable(name = "singularId") singularId: Long, @PathVariable(name = "firstCommentId") firstCommentId: Long, @PathVariable(name = "commentId") commentId: Long): String{
        if (!singularRepository.existsById(singularId)) throw Exception("Singular not found")
        if (!commentRepository.existsById(firstCommentId)) throw Exception("First comment not found")
        if (!secondCommentRepository.existsById(commentId)) throw Exception("Second comment not found")
        secondCommentRepository.deleteById(commentId)
        return "Comment deleted successfully"
    }

}