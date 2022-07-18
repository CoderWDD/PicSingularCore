package com.example.picsingularcore.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class Singular(
    @Id
    @Column(name = "singular_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var singularId: Long,

    @Column(name = "create_data")
    var pushData: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

    @Column(name = "like_count")
    var likeCount: Int,

    @Column(name = "comment_count")
    var commentCount: Int,

    @Column(name = "read_count")
    var readCount: Int,

    @Column(name = "description")
    var description: String,

    @Column(name = "singular_status")
    var singularStatus: String,

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    var imageList: List<ImageUrl>,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToMany
    @JoinTable(name = "singular_category_relation",
        joinColumns = [JoinColumn(name = "singular_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")])
    var categoryList: List<SingularCategory>,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "singular_id")
    @JsonIgnore
    var commentLevelFirstList: List<CommentLevelFirst>
)