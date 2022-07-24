package com.example.picsingularcore.pojo.dto

import org.springframework.lang.Nullable
import javax.validation.constraints.Pattern

data class UserUpdateDTO (
    @field:Nullable
    @field:Pattern(regexp = "^[a-zA-Z0-9]{6,24}$", message = "Username must be alphanumeric")
    val username: String? = null,

    @field:Nullable
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters, contain at least one lowercase letter, one uppercase letter, one number and one special character")
    val password: String? = null,

    @field:Nullable
    val avatar : String? = null,

    @field:Nullable
    val signature: String? = null,

        )