package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "holiday",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_store_date",
            columnNames = ["store_id", "date"]
        )
    ]
    )
class Holiday (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(length = 255)
    var reason: String?,

) : BaseEntity()