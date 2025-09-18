package com.carava.carwash.domain.address.repository

import com.carava.carwash.domain.address.entity.Address
import com.carava.carwash.domain.address.entity.AddressEntityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("addressRepository")
interface AddressRepository : JpaRepository<Address, Long> {

    fun findByEntityTypeAndEntityId(entityType: AddressEntityType, entityId: Long): Address?

}