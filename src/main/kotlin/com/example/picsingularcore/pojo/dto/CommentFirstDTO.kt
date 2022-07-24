package com.example.picsingularcore.pojo.dto

import com.google.gson.annotations.SerializedName
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CommentFirstDTO(
    @field:NotBlank(message = "comment content cannot be blank")
    val content: String,
    @field:NotNull(message = "Singular Id is required")
    val singularId: Long
) {
}