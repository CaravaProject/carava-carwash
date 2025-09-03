package com.carava.carwash.member.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*

@Entity(name = "member")
@Table(name = "owner_member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(name = "auth_id", nullable = false)
    var authId: Long
) : BaseEntity()
