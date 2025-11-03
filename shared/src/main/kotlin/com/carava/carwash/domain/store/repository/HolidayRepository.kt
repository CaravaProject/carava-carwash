package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.Holiday
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository("holidayRepository")
interface HolidayRepository : JpaRepository<Holiday, Long> {
    
    /**
     * 특정 날짜가 휴일인지 확인
     */
    @Query("""
        SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
        FROM Holiday h
        WHERE h.store.id = :storeId
        AND :date BETWEEN h.startDate AND h.endDate
    """)
    fun existsByStoreIdAndDate(
        @Param("storeId") storeId: Long,
        @Param("date") date: LocalDate
    ): Boolean
    
    /**
     * 특정 점포의 특정 기간 휴일 목록 조회
     */
    @Query("""
        SELECT h FROM Holiday h
        WHERE h.store.id = :storeId
        AND h.endDate >= :startDate
        AND h.startDate <= :endDate
        ORDER BY h.startDate ASC
    """)
    fun findByStoreIdAndDateRange(
        @Param("storeId") storeId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Holiday>
}
