package com.carava.carwash.common.notification.service

/**
 * 알림 서비스 인터페이스
 * FCM, Email, SMS 등 다양한 채널로 확장 가능
 */
interface NotificationService {
    /**
     * 개별 기기에 푸시 알림 전송
     * @param token FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    fun sendPush(token: String, title: String, body: String): Boolean
    
    /**
     * 특정 토픽에 푸시 알림 전송
     * @param topic 토픽 이름
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 전송 성공 여부
     */
    fun sendToTopic(topic: String, title: String, body: String): Boolean
    
    /**
     * 토픽 구독
     * @param token FCM 토큰
     * @param topic 구독할 토픽 이름
     * @return 구독 성공 여부
     */
    fun subscribeToTopic(token: String, topic: String): Boolean
}