package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.Singular
import com.example.picsingularcore.pojo.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface SingularRepository: JpaRepository<Singular,Long>, JpaSpecificationExecutor<Singular> {
    fun findBySingularStatusAndUser(singularStatus: String, user: User?): List<Singular>

    fun findBySingularStatus(singularStatus: String): List<Singular>

    fun findByUser(user: User?): List<Singular>

}