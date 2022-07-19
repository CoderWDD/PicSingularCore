package com.example.picsingularcore.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class User(
    @Id
    @Column(name = "user_id")
    @GeneratedValue(generator = "user_id_seq",strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    var userId: Long? = null,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "avatar", nullable = true)
    var avatar: String? = null,

    @Column(name = "signature",nullable = true)
    var signature: String? = null,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "ur_relation",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @JsonIgnore
    var roles: List<Role?>? = null,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "favorite_relation",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "singular_id")]
    )
    @JsonIgnore
    var favoriteList: List<Singular>? = null,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "subscription_relation",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "singular_id")]
    )
    @JsonIgnore
    var subscriptionList: List<Singular>? = null,
)