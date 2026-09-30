package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wedding_events")
data class WeddingEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val coupleNames: String,
    val eventDate: String,
    val venue: String,
    val coverImageRes: String, // Resource name or URI
    val pinCode: String = "",
    val totalPhotos: Int = 0,
    val selectionQuota: Int = 40,
    val photographerName: String = "পারফেক্ট ক্যাপচার ফটোগ্রাফি",
    val photographerPhone: String = "+8801700000000",
    val isWatermarked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wedding_photos")
data class WeddingPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val photoUri: String, // Resource name (e.g. "drawable/...") or content:// URI
    val category: String, // "BRIDE", "GROOM", "COUPLE", "RITUALS", "CANDID_GUESTS", "STAGE"
    val caption: String,
    val isSelected: Boolean = false,
    val selectionOrder: Int = 0,
    val clientNotes: String = "",
    val faceTags: String = "", // e.g. "BRIDE,GROOM", "ANANYA", "GUEST_GROUP"
    val detectedFaceCount: Int = 1,
    val faceFeatureSignature: String = "", // Compact serialized feature vector
    val isDownloaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "client_submissions")
data class ClientSubmission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val clientName: String,
    val clientPhone: String,
    val selectedCount: Int,
    val albumType: String,
    val specialNotes: String,
    val photoIdsSummary: String,
    val submittedAt: Long = System.currentTimeMillis()
)
