package com.example.picsingularcore.pojo

import org.springframework.lang.Nullable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class CommentLevelSecond(
    @Id
    @Column(name = "comment_second_id")
    @GeneratedValue(generator = "comment_second_id_seq",strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "comment_second_id_seq", sequenceName = "comment_second_id_seq", allocationSize = 1)
    var commentSecondId: Long? = null,

    @Column(name = "singular_id")
    var singularId: Long,

    @Column(name = "content")
    var content: String,

    @Column(name = "like_count")
    var likeCount: Int = 0,

    @Column(name = "replied_comment_id")
    val repliedCommentId: Long? = null,

    @Column(name = "replied_user_id")
    var repliedUserId: Long? = null,

    @Column(name = "replied_user_name")
    var repliedUserName: String? = null,

    @Column(name = "parent_comment_id")
    var parentCommentId: Long? = null,

    @Column(name = "parent_user_id")
    var parentUserId: Long? = null,

    @Column(name = "parent_user_name")
    var parentUserName: String? = null,

    @Column(name = "create_data")
    @Nullable
    var createData: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

    @Column(name = "user_id")
    var userId: Long,
    ){
    override fun equals(other: Any?): Boolean {
        return this.commentSecondId == (other as CommentLevelSecond).commentSecondId
    }
}