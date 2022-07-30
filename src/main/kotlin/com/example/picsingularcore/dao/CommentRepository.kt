package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.CommentLevelFirst
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository: JpaRepository<CommentLevelFirst,Long>,  JpaSpecificationExecutor<CommentLevelFirst> {
}