package com.carava.carwash.domain.auth.entity

import com.carava.carwash.common.constants.UserType
import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*

@Entity(name = "auth")
@Table(name = "auth")
class Auth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var userType: UserType

) : BaseEntity()