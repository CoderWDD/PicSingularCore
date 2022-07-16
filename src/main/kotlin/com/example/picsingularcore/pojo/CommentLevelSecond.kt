package com.example.picsingularcore.pojo

import org.springframework.lang.Nullable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class CommentLevelSecond(
    @Id
    @Column(name = "comment_second_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var commentSecondId: Long,

    @Column(name = "content")
    var content: String,

    @Column(name = "like_count")
    var likeCount: Int,

    @Column(name = "replied_user_id")
    var repliedUserId: Long,

    @Column(name = "create_data")
    @Nullable
    var createData: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "comment_level_first_id")
    var commentLevelFirstId: Long
    ) {

}