package com.example.picsingularcore.pojo.dto

import org.jetbrains.annotations.NotNull
import javax.validation.constraints.NotBlank

data class SingularDTO(
    @field:NotBlank(message = "Content is required")
    val content: String,
    @field:NotBlank(message = "Status is required")
    val status: String,
    @field:NotBlank(message = "Category is required")
    val category: List<String>,
    @field:NotBlank(message = "At least one image is required")
    val imagesUrl: List<String>,
    @field:NotBlank(message = "Singular title is required")
    val title : String
)