package com.carava.carwash.domain.reservation.entity

import com.carava.carwash.domain.store.entity.Menu
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "reservation_menus")
class ReservationMenu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    var reservation: Reservation? = null,

    @Column(nullable = false)
    val menuId: Long,

    @Column(nullable = false)
    val menuName: String,  // 스냅샷

    val menuDescription: String? = null,

    @Column(nullable = false)
    val unitPrice: BigDecimal,  // 예약 시점 가격

    @Column(nullable = false)
    val quantity: Int = 1,

    @Column(nullable = false)
    val totalPrice: BigDecimal,

    @Column(nullable = false)
    val duration: Int,  // 분 단위

    val categoryName: String? = null
) {
    companion object {
        // Menu로부터 ReservationMenu 생성
        fun fromMenu(menu: Menu, quantity: Int = 1): ReservationMenu {
            return ReservationMenu(
                menuId = menu.id,
                menuName = menu.name,
                menuDescription = menu.description,
                unitPrice = menu.price,
                quantity = quantity,
                totalPrice = menu.price.multiply(BigDecimal(quantity)),
                duration = menu.duration * quantity,
                categoryName = menu.category?.name
            )
        }
    }
}
