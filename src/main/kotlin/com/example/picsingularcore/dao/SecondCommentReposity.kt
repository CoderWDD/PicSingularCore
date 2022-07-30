package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.CommentLevelSecond
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface SecondCommentRepository : JpaRepository<CommentLevelSecond,Long>, JpaSpecificationExecutor<CommentLevelSecond> {

}