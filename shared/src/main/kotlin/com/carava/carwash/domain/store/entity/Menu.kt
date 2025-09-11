package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity(name = "menu")
@Table(name = "menu")
class Menu (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 255)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var duration: Int = 0,

    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    var displayOrder: Int = 0,

) : BaseEntity()