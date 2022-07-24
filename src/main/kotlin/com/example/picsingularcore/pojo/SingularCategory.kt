package com.example.picsingularcore.pojo

import javax.persistence.*

@Entity
data class SingularCategory(
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val categoryId: Long? = null,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "category_name")
    val categoryName: String,
)