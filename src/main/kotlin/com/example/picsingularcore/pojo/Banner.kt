package com.example.picsingularcore.pojo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class Banner(
    @Id
    @Column(name = "banner_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val bannerId: Long? = null,

    @Column(name = "banner_url")
    val bannerUrl: String,

    @Column(name = "pushDate")
    var pushDate: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),
)

