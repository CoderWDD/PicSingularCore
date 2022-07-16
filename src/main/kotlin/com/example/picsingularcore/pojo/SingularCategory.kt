package com.example.picsingularcore.pojo

import javax.persistence.*

@Entity
data class SingularCategory(
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val categoryId: Long,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "category_name")
    val categoryName: String,

    @ManyToMany
    @JoinTable(name = "singular_category_relation",
        joinColumns = [JoinColumn(name = "category_id")],
        inverseJoinColumns = [JoinColumn(name = "singular_id")])
    var singularList: List<Singular>
) {

}