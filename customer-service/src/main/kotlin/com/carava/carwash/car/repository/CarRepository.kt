package com.carava.carwash.car.repository

import com.carava.carwash.car.entity.Car
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CarRepository : JpaRepository<Car, Long> {
    
    // 특정 회원의 차량 목록 조회 (기본 차량 우선, 최신순)
    fun findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId: Long): List<Car>
    
    // 특정 회원의 기본 차량 조회
    fun findByMemberIdAndIsDefaultTrue(memberId: Long): Car?
    
    // 특정 회원이 소유한 차량인지 확인
    fun existsByIdAndMemberId(carId: Long, memberId: Long): Boolean
    
    // 차량번호 중복 확인 (같은 회원 내에서)
    fun existsByMemberIdAndLicensePlate(memberId: Long, licensePlate: String): Boolean
    
    // 차량번호 중복 확인 (수정 시 - 자기 자신 제외)
    fun existsByMemberIdAndLicensePlateAndIdNot(memberId: Long, licensePlate: String, carId: Long): Boolean
    
    // 특정 회원의 기본 차량을 모두 기본이 아닌 상태로 변경
    @Modifying
    @Query("UPDATE car c SET c.isDefault = false WHERE c.member.id = :memberId AND c.isDefault = true")
    fun updateAllDefaultToFalseByMemberId(@Param("memberId") memberId: Long)
    
    // 특정 회원의 차량 수 조회
    fun countByMemberId(memberId: Long): Long
}