package com.example.picsingularcore.pojo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class CommentLevelFirst(
    @Id
    @Column(name = "comment_first_id")
    @GeneratedValue(generator = "comment_first_id_seq",strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "comment_first_id_seq", sequenceName = "comment_first_id_seq", allocationSize = 1)
    var commentFirstId: Long? = null,

    @Column(name = "singular_id", nullable = false)
    var singularId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "content")
    var content: String? = null,

    @Column(name = "like_count")
    var likeCount: Int = 0,

    @Column(name = "create_data")
    var createDate: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var commentSecondList: MutableList<CommentLevelSecond>? = mutableListOf(),

    var avatar: String
)