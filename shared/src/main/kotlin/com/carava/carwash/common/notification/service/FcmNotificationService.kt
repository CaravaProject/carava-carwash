package com.carava.carwash.common.notification.service

import com.carava.carwash.common.notification.config.FcmConfig
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
@EnableConfigurationProperties(FcmConfig::class)
class FcmNotificationService(
    private val fcmConfig: FcmConfig
) : NotificationService {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    @PostConstruct
    fun init() {
        if (!fcmConfig.enabled) {
            logger.info("FCM 비활성화 상태")
            return
        }
        
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                val resource = ClassPathResource(fcmConfig.credentialsPath)
                
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
                    .build()
                
                FirebaseApp.initializeApp(options)
                logger.info("FCM 초기화 성공")
            }
        } catch (e: Exception) {
            logger.error("FCM 초기화 실패: ${e.message}", e)
        }
    }
    
    override fun sendPush(token: String, title: String, body: String): Boolean {
        if (!fcmConfig.enabled) {
            logger.debug("FCM 비활성화 - 푸시 전송 스킵")
            return true
        }
        
        val message = Message.builder()
            .setToken(token)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()
        
        return try {
            val messageId = FirebaseMessaging.getInstance().send(message)
            logger.info("푸시 전송 성공: $messageId")
            true
        } catch (e: Exception) {
            logger.error("푸시 전송 실패: ${e.message}", e)
            false
        }
    }
    
    override fun sendToTopic(topic: String, title: String, body: String): Boolean {
        if (!fcmConfig.enabled) {
            logger.debug("FCM 비활성화 - 토픽 전송 스킵")
            return true
        }
        
        val message = Message.builder()
            .setTopic(topic)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()
        
        return try {
            val messageId = FirebaseMessaging.getInstance().send(message)
            logger.info("토픽 메시지 전송 성공 - Topic: $topic, MessageId: $messageId")
            true
        } catch (e: Exception) {
            logger.error("토픽 메시지 전송 실패 - Topic: $topic, Error: ${e.message}", e)
            false
        }
    }
    
    override fun subscribeToTopic(token: String, topic: String): Boolean {
        if (!fcmConfig.enabled) {
            logger.debug("FCM 비활성화 - 토픽 구독 스킵")
            return true
        }
        
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(listOf(token), topic)
            logger.info("토픽 구독 성공 - Topic: $topic")
            true
        } catch (e: Exception) {
            logger.error("토픽 구독 실패 - Topic: $topic, Error: ${e.message}", e)
            false
        }
    }
}