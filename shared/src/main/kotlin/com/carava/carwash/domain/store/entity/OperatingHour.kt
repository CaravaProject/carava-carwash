package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "operating_hour",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_store_day_of_week",
            columnNames = ["store_id", "day_of_week"]
        )
    ]
    )
class OperatingHour (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    var store: Store,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: DayOfWeek,

    @Column(name = "open_time")
    var openTime: LocalTime?,

    @Column(name = "close_time")
    var closeTime: LocalTime?,

    @Column(name = "is_open", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    var isOpen: Boolean = true,

) : BaseEntity()