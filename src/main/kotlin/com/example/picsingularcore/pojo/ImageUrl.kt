package com.example.picsingularcore.pojo

import javax.persistence.*

@Entity
data class ImageUrl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    var imageId: Long,

    @Column(name = "singular_id")
    var singularId: String,

    @Column(name = "image_url")
    var imageUrl: String? = null
)