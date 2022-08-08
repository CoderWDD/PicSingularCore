package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    fun findByUsernameAndPassword(username: String,password: String): User?
    fun findByUsername(username: String?): User?

}