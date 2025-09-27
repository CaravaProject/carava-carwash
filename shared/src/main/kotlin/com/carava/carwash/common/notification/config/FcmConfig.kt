package com.carava.carwash.common.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * FCM 설정 클래스
 * application.yml의 fcm 프로퍼티와 매핑됨
 */
@ConfigurationProperties(prefix = "fcm")
data class FcmConfig(
    /**
     * FCM 활성화 여부
     * - true: FCM 초기화 및 푸시 전송
     * - false: FCM 비활성화 (로컬 개발용)
     */
    val enabled: Boolean = true,
    
    /**
     * Firebase 서비스 계정 키 파일 경로
     * - 기본값: carvana-firebase-service-account.json
     * - shared/src/main/resources에 위치
     */
    val credentialsPath: String = "carvana-firebase-service-account.json"
)