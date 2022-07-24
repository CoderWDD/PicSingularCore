package com.example.picsingularcore.pojo.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CommentSecondDTO(
    @field:NotBlank(message = "Content cannot be blank")
    val content: String,

    @field:NotNull(message = "Parent comment id cannot be null")
    val parentCommentId: Long,

    @field:NotNull(message = "Parent user id cannot be null")
    var parentUserId: Long,

    @field:NotNull(message = "Reply comment id cannot be null")
    val replyCommentId: Long,

    @field:NotNull(message = "Reply user id cannot be null")
    val replyUserId: Long,

    @field:NotNull(message = "Singular id cannot be null")
    val singularId: Long
)