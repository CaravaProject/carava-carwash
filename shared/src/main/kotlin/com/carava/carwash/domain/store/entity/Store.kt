package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import com.carava.carwash.common.exception.ForbiddenException
import jakarta.persistence.*

@Entity(name = "store")
@Table(name = "store")
class Store (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var ownerMemberId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: StoreCategory = StoreCategory.CAR_WASH,

) : BaseEntity() {

    fun validateOwnership(requestMemberId: Long) {
        if (this.ownerMemberId != requestMemberId) {
            throw ForbiddenException("해당 가게에 대한 권한이 없습니다.")
        }
    }

}