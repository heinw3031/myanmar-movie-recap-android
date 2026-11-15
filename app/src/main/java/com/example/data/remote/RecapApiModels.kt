package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateJobResponse(
    val jobId: String? = null,
    val id: String? = null,
    val status: String? = null,
    val progress: Int? = null,
    val message: String? = null,
    val error: String? = null
) {
    fun resolvedJobId(): String? = jobId ?: id
}

@JsonClass(generateAdapter = true)
data class SceneDto(
    val sceneNumber: Int = 1,
    val sourceStartSec: Double = 0.0,
    val sourceEndSec: Double = 0.0,
    val recapStartSec: Double? = null,
    val recapEndSec: Double? = null,
    val narrationMy: String = ""
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    val jobId: String? = null,
    val id: String? = null,
    val status: String = "",
    val progress: Double? = null,
    val title: String? = null,
    val category: String? = null,
    val lengthMinutes: Int? = null,
    val scenes: List<SceneDto>? = null,
    val finalVideoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val durationSec: Double? = null,
    val error: String? = null,
    val message: String? = null,
    val createdAt: String? = null
) {
    fun resolvedJobId(): String = jobId ?: id ?: ""
    fun resolvedProgressInt(): Int = (progress ?: 0.0).toInt().coerceIn(0, 100)
}

@JsonClass(generateAdapter = true)
data class JobListWrapper(
    val jobs: List<JobStatusResponse>? = null,
    val data: List<JobStatusResponse>? = null
)

@JsonClass(generateAdapter = true)
data class SceneApproveDto(
    val sceneNumber: Int,
    val sourceStartSec: Int,
    val sourceEndSec: Int,
    val narrationMy: String
)

@JsonClass(generateAdapter = true)
data class ApproveScriptRequest(
    val title: String,
    val scenes: List<SceneApproveDto>
)

@JsonClass(generateAdapter = true)
data class ErrorBodyDto(
    val error: String? = null,
    val message: String? = null,
    val detail: String? = null
)
