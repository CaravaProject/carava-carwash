package com.carava.carwash.car.service

import com.carava.carwash.car.dto.CarListResponseDto
import com.carava.carwash.car.dto.CarResponseDto
import com.carava.carwash.car.dto.CreateCarRequestDto
import com.carava.carwash.car.dto.UpdateCarRequestDto
import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.repository.CarRepository
import com.carava.carwash.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CarService(
    private val carRepository: CarRepository,
    private val memberRepository: MemberRepository
) {
    
    /**
     * 차량 등록
     */
    @Transactional
    fun createCar(memberId: Long, request: CreateCarRequestDto): CarResponseDto {
        val member = memberRepository.findByIdOrNull(memberId) 
            ?: throw IllegalArgumentException("존재하지 않는 회원입니다.")

        // 차량번호 중복 확인
        if (carRepository.existsByMemberIdAndLicensePlate(memberId, request.licensePlate)) {
            throw IllegalArgumentException("이미 등록된 차량번호입니다.")
        }

        // 차량 수 조회
        val carCount = carRepository.countByMemberId(memberId)

        val isDefault = if (carCount == 0L) {
            true  // 첫 번째 차량은 무조건 기본 차량
        } else {
            // 두 번째 차량부터는 사용자 선택에 따라
            if (request.isDefault) {
                carRepository.updateAllDefaultToFalseByMemberId(memberId)
            }
            request.isDefault
        }

        val car = Car(
            member = member,
            brand = request.brand,
            model = request.model,
            year = request.year,
            color = request.color,
            licensePlate = request.licensePlate,
            carType = request.carType,
            isDefault = isDefault
        )

        val savedCar = carRepository.save(car)
        return savedCar.toResponseDto()
    }

    /**
     * 차량 목록 조회
     */
    fun getCarsByMemberId(memberId: Long): CarListResponseDto {
        val cars = carRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId)
        return CarListResponseDto(
            cars = cars.map { it.toResponseDto() },
            totalCount = cars.size
        )
    }

    /**
     * 차량 상세 조회
     */
    fun getCarById(memberId: Long, carId: Long): CarResponseDto {
        val car = carRepository.findByIdOrNull(carId) 
            ?: throw IllegalArgumentException("존재하지 않는 차량입니다.")

        if (car.member.id != memberId) {
            throw IllegalArgumentException("접근 권한이 없습니다.")
        }
        return car.toResponseDto()
    }

    /**
     * 차량 정보 수정
     */
    @Transactional
    fun updateCar(memberId: Long, carId: Long, request: UpdateCarRequestDto): CarResponseDto {
        val car = carRepository.findByIdOrNull(carId) 
            ?: throw IllegalArgumentException("존재하지 않는 차량입니다.")

        if (car.member.id != memberId) {
            throw IllegalArgumentException("접근 권한이 없습니다.")
        }

        // 차량번호 중복 확인 (자기 자신 제외)
        if (carRepository.existsByMemberIdAndLicensePlateAndIdNot(memberId, request.licensePlate, carId)) {
            throw IllegalArgumentException("이미 등록된 차량번호입니다.")
        }

        // 기본 차량 설정 처리
        if (request.isDefault && !car.isDefault) {
            carRepository.updateAllDefaultToFalseByMemberId(memberId)
        }

        car.apply {
            brand = request.brand
            model = request.model
            year = request.year
            color = request.color
            licensePlate = request.licensePlate
            carType = request.carType
            isDefault = request.isDefault
        }

        return car.toResponseDto()
    }

    /**
     * 차량 삭제
     */
    @Transactional
    fun deleteCar(memberId: Long, carId: Long) {
        val car = carRepository.findByIdOrNull(carId)
            ?: throw IllegalArgumentException("존재하지 않는 차량입니다.")

        if (car.member.id != memberId) {
            throw IllegalArgumentException("접근 권한이 없습니다.")
        }

        val wasDefault = car.isDefault
        carRepository.delete(car)

        // 삭제된 차량이 기본 차량이었다면, 가장 최근에 등록된 차량을 기본으로 설정
        if (wasDefault) {
            val remainingCars = carRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId)
            if (remainingCars.isNotEmpty()) {
                remainingCars.first().isDefault = true  // 더티 체킹으로 자동 UPDATE
            }
        }
    }

    /**
     * 기본 차량 설정
     */
    @Transactional
    fun setDefaultCar(memberId: Long, carId: Long): CarResponseDto {
        val car = carRepository.findByIdOrNull(carId)
            ?: throw IllegalArgumentException("존재하지 않는 차량입니다.")

        if (car.member.id != memberId) {
            throw IllegalArgumentException("접근 권한이 없습니다.")
        }

        // 기존 기본 차량들을 모두 해제
        carRepository.updateAllDefaultToFalseByMemberId(memberId)

        // 선택한 차량을 기본으로 설정
        car.isDefault = true  // 더티 체킹으로 자동 UPDATE

        return car.toResponseDto()
    }
}

// 확장 함수 - Car 엔티티를 CarResponseDto로 변환
private fun Car.toResponseDto(): CarResponseDto {
    return CarResponseDto(
        id = this.id,
        brand = this.brand,
        model = this.model,
        year = this.year,
        color = this.color,
        licensePlate = this.licensePlate,
        carType = this.carType,
        isDefault = this.isDefault,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}