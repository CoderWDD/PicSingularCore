package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository: JpaRepository<Role,Long> {
    fun findByName(name: String): Role?
}