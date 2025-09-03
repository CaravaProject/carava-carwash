package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "operating_hour")
class OperatingHour (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    var store: Store,

    @Enumerated(EnumType.STRING)
    var dayOfWeek: DayOfWeek,

    var openTime: LocalTime?,

    var closeTime: LocalTime?,

    var isOpen: Boolean = true,

) : BaseEntity()