package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.OperatingHour
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.DayOfWeek

interface OperatingHourRepository : JpaRepository<OperatingHour, Long> {

    fun deleteByStoreId(storeId: Long)
    
    /**
     * 특정 점포의 특정 요일 영업시간 조회
     */
    @Query("""
        SELECT oh FROM OperatingHour oh
        WHERE oh.store.id = :storeId
        AND oh.dayOfWeek = :dayOfWeek
    """)
    fun findByStoreIdAndDayOfWeek(
        @Param("storeId") storeId: Long,
        @Param("dayOfWeek") dayOfWeek: DayOfWeek
    ): OperatingHour?
    
    /**
     * 특정 점포의 모든 영업시간 조회
     */
    @Query("""
        SELECT oh FROM OperatingHour oh
        WHERE oh.store.id = :storeId
        ORDER BY oh.dayOfWeek ASC
    """)
    fun findAllByStoreId(
        @Param("storeId") storeId: Long
    ): List<OperatingHour>
}
