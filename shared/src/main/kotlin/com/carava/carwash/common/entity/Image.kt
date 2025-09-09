package com.carava.carwash.common.entity

import jakarta.persistence.*

@Entity
@Table(name = "image")
class Image (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var entityType: EntityType,

    @Column(nullable = false)
    var entityId: Long,

    @Column(nullable = false, length = 500)
    var imagePath: String,

    @Column(length = 500)
    var thumbnailPath: String? = null,

    @Column(length = 500)
    var mediumPath: String? = null,

    @Column(columnDefinition = "INT DEFAULT 0")
    var displayOrder: Int = 0,

    // 메타데이터
    @Column(length = 255)
    var originalFileName: String? = null,

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    var fileSize: Long = 0,

    @Column(length = 50)
    var mimeType: String? = null


) : BaseEntity()