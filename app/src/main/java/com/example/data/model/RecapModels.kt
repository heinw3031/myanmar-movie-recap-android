package com.example.data.model

import com.example.data.remote.SceneDto

val RECAP_CATEGORIES = listOf(
    "Hollywood",
    "Korean",
    "Thai",
    "Chinese",
    "Myanmar",
    "Horror",
    "Action",
    "Thriller"
)

val RECAP_LENGTH_OPTIONS = listOf(5, 10, 15, 30)

data class RecapScene(
    val sceneNumber: Int,
    val sourceStartSec: Int,
    val sourceEndSec: Int,
    val recapStartSec: Double? = null,
    val recapEndSec: Double? = null,
    val narrationMy: String
)

fun SceneDto.toDomain(): RecapScene = RecapScene(
    sceneNumber = sceneNumber,
    sourceStartSec = sourceStartSec.toInt().coerceAtLeast(0),
    sourceEndSec = sourceEndSec.toInt().coerceAtLeast(sourceStartSec.toInt()),
    recapStartSec = recapStartSec,
    recapEndSec = recapEndSec,
    narrationMy = narrationMy
)

data class RecapItem(
    val jobId: String,
    val title: String,
    val category: String,
    val lengthMinutes: Int,
    val durationSec: Double,
    val finalVideoUrl: String,
    val thumbnailUrl: String?,
    val scenes: List<RecapScene>,
    val isPublished: Boolean,
    val isDraft: Boolean,
    val isSaved: Boolean,
    val isLiked: Boolean,
    val lastWatchedAt: Long?,
    val createdAt: Long,
    val viewCount: Int
)
