package com.example.picsingularcore.pojo

import javax.persistence.*

@Entity
data class Role(
    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var role_id: Long,
    @Column(name = "role_name")
    var name: String,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.DETACH])
    var users: List<User>
)