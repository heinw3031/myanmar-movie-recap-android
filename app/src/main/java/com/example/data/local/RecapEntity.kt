package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recaps")
data class RecapEntity(
    @PrimaryKey
    val jobId: String,
    val title: String,
    val category: String,
    val lengthMinutes: Int,
    val durationSec: Double,
    val finalVideoUrl: String,
    val thumbnailUrl: String?,
    val scenesJson: String,
    val isPublished: Boolean,
    val isDraft: Boolean,
    val isSaved: Boolean,
    val isLiked: Boolean,
    val lastWatchedAt: Long?,
    val createdAt: Long,
    val viewCount: Int
)
