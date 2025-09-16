package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.Holiday
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("holidayRepository")
interface HolidayRepository : JpaRepository<Holiday, Long> {
}