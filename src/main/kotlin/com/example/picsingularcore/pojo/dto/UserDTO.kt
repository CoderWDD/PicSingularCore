package com.example.picsingularcore.pojo.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

data class UserDTO(
    @field:NotBlank(message = "Username is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9]{6,24}$", message = "Username must be alphanumeric")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters, contain at least one lowercase letter, one uppercase letter, one number and one special character")
    val password: String
)