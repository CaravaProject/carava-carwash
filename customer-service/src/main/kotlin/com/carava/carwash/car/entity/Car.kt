package com.carava.carwash.car.entity

import com.carava.carwash.common.entity.BaseEntity
import com.carava.carwash.member.entity.Member
import jakarta.persistence.*

@Entity(name = "car")
@Table(
    name = "car",
    indexes = [
        Index(name = "idx_customer_member_id", columnList = "customer_member_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_customer_license_plate", columnNames = ["customer_member_id", "license_plate"])
    ]
)
class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_member_id", nullable = false)
    var member: Member,

    @Column(nullable = false)
    var brand: String,

    @Column(nullable = false)
    var model: String,

    @Column(nullable = false)
    var year: Int,

    @Column  // nullable = true (기본값)
    var color: String? = null,

    @Column(name = "license_plate", nullable = false)
    var licensePlate: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false)
    var carType: CarType,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false
) : BaseEntity()

enum class CarType {
    SEDAN, SUV, HATCHBACK, COUPE, CONVERTIBLE, TRUCK, VAN
}