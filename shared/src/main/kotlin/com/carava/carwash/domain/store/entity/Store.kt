package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import com.carava.carwash.common.exception.ForbiddenException
import jakarta.persistence.*
import java.math.BigDecimal

@Entity(name = "store")
@Table(name = "store")
class Store (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var ownerMemberId: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 20)
    var phone: String?= null,

    //TODO: 주소 entity 만들고 연결
    @Column(nullable = false)
    var addressId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: StoreCategory = StoreCategory.CAR_WASH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StoreStatus = StoreStatus.ACTIVE,

    @Column(precision = 2, scale = 1)
    var averageRating: BigDecimal = BigDecimal("0.0"),

    @Column(columnDefinition = "INT DEFAULT 0")
    var totalReviews: Int = 0,

    @Column(columnDefinition = "INT DEFAULT 0")
    var viewCount: Int = 0,

    @Column(columnDefinition = "INT DEFAULT 0")
    var favoriteCount: Int = 0,

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var menus: MutableList<Menu> = mutableListOf(),

) : BaseEntity() {

    fun validateOwnership(requestMemberId: Long) {
        if (this.ownerMemberId != requestMemberId) {
            throw ForbiddenException("해당 가게에 대한 권한이 없습니다.")
        }
    }

    fun addMenu(menu: Menu) {
        menus.add(menu)
        menu.store = this
    }

}