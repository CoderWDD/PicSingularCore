package com.example.picsingularcore.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import javax.persistence.*

@Entity
data class Singular(
    @Id
    @Column(name = "singular_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var singularId: Long? = null,

    @Column(name = "create_data")
    var pushDate: String = LocalDateTime.now()
        .format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()),

    @Column(name = "like_count")
    var likeCount: Int = 0,

    @Column(name = "comment_count")
    var commentCount: Int = 0,

    @Column(name = "read_count")
    var readCount: Int = 0,

    @Column(name = "description")
    var description: String = "",

    @Column(name = "singular_status")
    var singularStatus: String = "Save",

    @OneToMany(fetch = FetchType.LAZY,cascade = [CascadeType.ALL])
    var imageList: List<ImageUrl>? = null,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH])
    @JsonIgnore
    var user: User? = null,

    @ManyToMany(cascade = [CascadeType.DETACH,CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH], fetch = FetchType.LAZY)
    @JoinTable(name = "singular_category_relation",
        joinColumns = [JoinColumn(name = "singular_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")])
    @JsonIgnore
    var categoryList: MutableList<SingularCategory>? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "singular_id")
    @JsonIgnore
    var commentLevelFirstList: MutableList<CommentLevelFirst>? = null,
){
    override fun equals(other: Any?): Boolean {
        return this.singularId == (other as Singular).singularId
    }
}