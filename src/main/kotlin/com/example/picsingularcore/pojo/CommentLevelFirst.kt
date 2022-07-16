package com.example.picsingularcore.pojo

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

    @ManyToMany
    @JoinTable(name = "comment_level_first",
            joinColumns = [JoinColumn(name = "user_id")],
            inverseJoinColumns = [JoinColumn(name = "singular_id")])
    var singularList: List<Singular>,

    @OneToMany
    @JoinColumn(name = "comment_second_id")
    var commentSecondList: List<CommentLevelSecond>
)