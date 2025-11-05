package com.carava.carwash.domain.address.entity

import com.carava.carwash.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "address")
class Address (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    var entityType: AddressEntityType,

    @Column(name = "entity_id", nullable = false)
    var entityId: Long,

    @Column(length = 5, nullable = false)
    var zonecode: String,

    @Column(name = "road_address", length = 200)
    var roadAddress: String?,

    @Column(name = "jibun_address", length = 200)
    var jibunAddress: String?,

    @Column(name = "user_selected_type", length = 1)
    var userSelectedType: String,

    @Column(length = 20)
    var sido: String?,

    @Column(length = 30)
    var sigungu: String?,

    @Column(length = 20)
    var bname: String?,

    @Column(name = "building_name", length = 30)
    var buildingName: String?,

    @Column(name = "detail_address", length = 100)
    var detailAddress: String?,

    ) : BaseEntity(){

    val selectedAddress: String
        get() = when(userSelectedType) {
            "J" -> jibunAddress.orEmpty()
            else -> roadAddress.orEmpty()
        }

    val fullAddress: String
        get() = when {
            detailAddress.isNullOrBlank() -> selectedAddress
            else -> "$selectedAddress $detailAddress"
        }
}