package com.carava.carwash.domain.reservation.entity

enum class ReservationStatus(val description: String) {
    PENDING("대기중"),          // 점주 승인 대기
    CONFIRMED("확정"),          // 예약 확정
    REJECTED("거절"),           // 점주가 거절
    IN_PROGRESS("진행중"),      // 서비스 진행중
    COMPLETED("완료"),          // 서비스 완료
    CANCELLED("취소"),          // 고객/점주 취소
    NO_SHOW("노쇼");           // 고객 미방문
    
    // 종료 상태인지 확인
    fun isTerminal(): Boolean {
        return this in listOf(REJECTED, COMPLETED, CANCELLED, NO_SHOW)
    }
    
    // 활성 상태인지 확인
    fun isActive(): Boolean {
        return this in listOf(PENDING, CONFIRMED, IN_PROGRESS)
    }
}
