package com.example.picsingularcore.pojo

import javax.persistence.*

@Entity
data class Role(
    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var role_id: Long? = null,
    @Column(name = "role_name")
    var name: String?,
){
    override fun equals(other: Any?): Boolean {
        return this.name == (other as Role).name
    }
}