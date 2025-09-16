package com.carava.carwash.common.entity

import jakarta.persistence.*

@Entity
@Table(name = "image")
class Image (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    var entityType: ImageEntityType,

    @Column(name = "entity_id", nullable = false)
    var entityId: Long,

    @Column(name = "image_path", nullable = false, length = 500)
    var imagePath: String,

    @Column(name = "thumbnail_path", length = 500)
    var thumbnailPath: String? = null,

    @Column(name = "medium_path", length = 500)
    var mediumPath: String? = null,

    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    var displayOrder: Int = 0,

    @Column(name = "original_file_name", length = 255)
    var originalFileName: String? = null,

    @Column(name = "file_size", columnDefinition = "BIGINT DEFAULT 0")
    var fileSize: Long = 0,

    @Column(name = "mime_type", length = 50)
    var mimeType: String? = null


) : BaseEntity()