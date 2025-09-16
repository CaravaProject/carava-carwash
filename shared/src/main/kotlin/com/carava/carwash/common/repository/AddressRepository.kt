package com.carava.carwash.common.repository

import com.carava.carwash.common.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("addressRepository")
interface AddressRepository: JpaRepository<Address, Long> {
}