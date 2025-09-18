package com.carava.carwash.store.dto

import com.carava.carwash.domain.address.entity.Address
import com.carava.carwash.domain.address.entity.AddressEntityType

data class AddressDto(
    val zonecode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val userSelectedType: String,
    val sido: String?,
    val sigungu: String?,
    val bname: String?,
    val buildingName: String?,
    val detailAddress: String?
) {
    fun toEntity(entityId: Long, entityType: AddressEntityType): Address = Address(
        zonecode = zonecode,
        roadAddress = roadAddress,
        jibunAddress = jibunAddress,
        userSelectedType = userSelectedType,
        sido = sido,
        sigungu = sigungu,
        bname = bname,
        buildingName = buildingName,
        detailAddress = detailAddress,
        entityId = entityId,
        entityType = entityType
    )
}