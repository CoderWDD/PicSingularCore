package com.example.picsingularcore.pojo.dto

import javax.persistence.Column

data class UserInfoDTO (
    var userId: Long,

    var username: String,

    var avatar: String? = null,

    var signature: String? = null
)