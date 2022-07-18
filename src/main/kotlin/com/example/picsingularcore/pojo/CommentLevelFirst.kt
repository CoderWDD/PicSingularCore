package com.example.picsingularcore.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.lang.Nullable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class CommentLevelFirst(
    @Id
    @Column(name = "comment_first_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var commentFirstId: Long,

    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "content")
    var content: String,

    @Column(name = "like_count")
    var likeCount: Int,

    @Column(name = "create_data")
    @Nullable
    var createData: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

//    @ManyToOne
//    @JsonIgnore
//    @JoinColumn(name = "singular_id")
//    var singular: Singular? = null

//    @OneToMany( cascade = [CascadeType.DETACH])
//    @Column(name = "comment_second_list")
//    var commentSecondList: List<CommentLevelSecond>
)