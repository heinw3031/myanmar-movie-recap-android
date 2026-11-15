package com.example.data.repository

import com.example.data.local.RecapDao
import com.example.data.local.RecapEntity
import com.example.data.model.RecapItem
import com.example.data.model.RecapScene
import com.example.data.model.toDomain
import com.example.data.remote.ApproveScriptRequest
import com.example.data.remote.BackendApiClient
import com.example.data.remote.JobStatusResponse
import com.example.data.remote.ProgressRequestBody
import com.example.data.remote.SceneApproveDto
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecapScriptRepository(
    private val recapDao: RecapDao,
    private val apiClient: BackendApiClient
) {
    private val scenesAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, RecapScene::class.java)
        apiClient.moshi.adapter<List<RecapScene>>(type)
    }

    val allRecapsFlow: Flow<List<RecapItem>> = recapDao.observeAllRecaps().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun uploadVideoJob(
        baseUrl: String,
        videoFile: File,
        displayName: String,
        mimeType: String,
        lengthMinutes: Int,
        category: String,
        onUploadProgress: (bytesWritten: Long, totalBytes: Long) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val service = apiClient.getService(baseUrl)
            ?: return@withContext Result.failure(
                IllegalStateException("Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ")
            )

        try {
            val mediaType = mimeType.toMediaTypeOrNull() ?: "video/mp4".toMediaTypeOrNull()
            val progressBody = ProgressRequestBody(
                file = videoFile,
                contentType = mediaType,
                onProgress = onUploadProgress
            )
            val safeFileName = displayName.ifBlank { videoFile.name }
            val videoPart = MultipartBody.Part.createFormData("video", safeFileName, progressBody)
            val lengthBody = lengthMinutes.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = service.createJob(
                video = videoPart,
                lengthMinutes = lengthBody,
                category = categoryBody
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException(
                        apiClient.extractErrorMessage(response, "ဗီဒီယို upload တင်ခြင်း မအောင်မြင်ပါ")
                    )
                )
            }

            val body = response.body()
            val jobId = body?.resolvedJobId()
            if (jobId.isNullOrBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(body?.error ?: "Server မှ jobId ပြန်မပေးပါ")
                )
            }
            Result.success(jobId)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Upload ချိတ်ဆက်မှု အမှား: ${e.localizedMessage ?: "Unknown error"}")
            )
        }
    }

    suspend fun fetchJobStatus(
        baseUrl: String,
        jobId: String
    ): Result<JobStatusResponse> = withContext(Dispatchers.IO) {
        val service = apiClient.getService(baseUrl)
            ?: return@withContext Result.failure(
                IllegalStateException("Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ")
            )

        try {
            val response = service.getJobStatus(jobId)
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException(
                        apiClient.extractErrorMessage(response, "Job အခြေအနေ စစ်ဆေးမရပါ")
                    )
                )
            }
            val body = response.body()
                ?: return@withContext Result.failure(IllegalStateException("Server မှ အချက်အလက် မရရှိပါ"))
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Server ချိတ်ဆက်မှု အမှား: ${e.localizedMessage ?: "Network error"}")
            )
        }
    }

    suspend fun approveScript(
        baseUrl: String,
        jobId: String,
        title: String,
        scenes: List<RecapScene>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val service = apiClient.getService(baseUrl)
            ?: return@withContext Result.failure(
                IllegalStateException("Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ")
            )

        try {
            val request = ApproveScriptRequest(
                title = title.trim(),
                scenes = scenes.map { scene ->
                    SceneApproveDto(
                        sceneNumber = scene.sceneNumber,
                        sourceStartSec = scene.sourceStartSec,
                        sourceEndSec = scene.sourceEndSec,
                        narrationMy = scene.narrationMy.trim()
                    )
                }
            )
            val response = service.approveScript(jobId, request)
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException(
                        apiClient.extractErrorMessage(response, "ဇာတ်ညွှန်း အတည်ပြုခြင်း မအောင်မြင်ပါ")
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("ဇာတ်ညွှန်း အတည်ပြုမှု အမှား: ${e.localizedMessage ?: "Network error"}")
            )
        }
    }

    suspend fun startGenerate(
        baseUrl: String,
        jobId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val service = apiClient.getService(baseUrl)
            ?: return@withContext Result.failure(
                IllegalStateException("Backend မချိတ်ရသေးပါ — အသံနှင့် video အစစ် မထုတ်နိုင်သေးပါ")
            )

        try {
            val response = service.startGenerate(jobId)
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException(
                        apiClient.extractErrorMessage(response, "Recap ထုတ်လုပ်မှု စတင်မရပါ")
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Generate စတင်မှု အမှား: ${e.localizedMessage ?: "Network error"}")
            )
        }
    }

    suspend fun syncCompletedJobsFromBackend(baseUrl: String) = withContext(Dispatchers.IO) {
        val service = apiClient.getService(baseUrl) ?: return@withContext
        try {
            val response = service.listJobsRaw()
            if (!response.isSuccessful) return@withContext
            val rawJson = response.body()?.string() ?: return@withContext
            val jobs = apiClient.parseJobListJson(rawJson)
            jobs.filter { it.status.equals("done", ignoreCase = true) }.forEach { job ->
                val id = job.resolvedJobId()
                if (id.isNotBlank()) {
                    val existing = recapDao.getRecapById(id)
                    val videoUrl = apiClient.resolveUrl(
                        baseUrl,
                        job.finalVideoUrl?.takeIf { it.isNotBlank() } ?: "/api/jobs/$id/video"
                    )
                    val scenes = job.scenes?.map { it.toDomain() } ?: emptyList()
                    val entity = RecapEntity(
                        jobId = id,
                        title = job.title?.takeIf { it.isNotBlank() } ?: "ရုပ်ရှင် Recap #$id",
                        category = job.category?.takeIf { it.isNotBlank() } ?: "Hollywood",
                        lengthMinutes = job.lengthMinutes ?: 10,
                        durationSec = job.durationSec ?: scenes.lastOrNull()?.recapEndSec ?: (job.lengthMinutes ?: 10) * 60.0,
                        finalVideoUrl = videoUrl,
                        thumbnailUrl = job.thumbnailUrl?.let { apiClient.resolveUrl(baseUrl, it) },
                        scenesJson = scenesAdapter.toJson(scenes),
                        isPublished = existing?.isPublished ?: true,
                        isDraft = existing?.isDraft ?: false,
                        isSaved = existing?.isSaved ?: false,
                        isLiked = existing?.isLiked ?: false,
                        lastWatchedAt = existing?.lastWatchedAt,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                        viewCount = existing?.viewCount ?: 0
                    )
                    recapDao.upsertRecap(entity)
                }
            }
        } catch (_: Exception) {
            // Backend list endpoint is optional; keep local Room store intact
        }
    }

    suspend fun saveCompletedRecap(
        baseUrl: String,
        job: JobStatusResponse,
        fallbackTitle: String,
        fallbackCategory: String,
        fallbackLengthMinutes: Int,
        fallbackScenes: List<RecapScene>,
        asPublished: Boolean
    ): RecapItem = withContext(Dispatchers.IO) {
        val id = job.resolvedJobId()
        val existing = recapDao.getRecapById(id)
        val resolvedScenes = job.scenes?.takeIf { it.isNotEmpty() }?.map { it.toDomain() } ?: fallbackScenes
        val videoUrl = apiClient.resolveUrl(
            baseUrl,
            job.finalVideoUrl?.takeIf { it.isNotBlank() } ?: "/api/jobs/$id/video"
        )
        val thumbUrl = job.thumbnailUrl?.let { apiClient.resolveUrl(baseUrl, it) }
        val duration = job.durationSec
            ?: resolvedScenes.lastOrNull()?.recapEndSec
            ?: (fallbackLengthMinutes * 60.0)

        val entity = RecapEntity(
            jobId = id,
            title = job.title?.takeIf { it.isNotBlank() } ?: fallbackTitle.ifBlank { "ရုပ်ရှင် Recap" },
            category = job.category?.takeIf { it.isNotBlank() } ?: fallbackCategory,
            lengthMinutes = job.lengthMinutes ?: fallbackLengthMinutes,
            durationSec = duration,
            finalVideoUrl = videoUrl,
            thumbnailUrl = thumbUrl,
            scenesJson = scenesAdapter.toJson(resolvedScenes),
            isPublished = if (asPublished) true else (existing?.isPublished ?: false),
            isDraft = if (!asPublished) true else false,
            isSaved = existing?.isSaved ?: false,
            isLiked = existing?.isLiked ?: false,
            lastWatchedAt = existing?.lastWatchedAt,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            viewCount = existing?.viewCount ?: 0
        )
        recapDao.upsertRecap(entity)
        entity.toDomain()
    }

    suspend fun publishRecap(jobId: String) = recapDao.markPublished(jobId)

    suspend fun saveDraft(jobId: String) = recapDao.markDraft(jobId)

    suspend fun toggleSaved(jobId: String, saved: Boolean) = recapDao.setSaved(jobId, saved)

    suspend fun toggleLiked(jobId: String, liked: Boolean) = recapDao.setLiked(jobId, liked)

    suspend fun recordWatch(jobId: String) = recapDao.recordWatch(jobId, System.currentTimeMillis())

    suspend fun deleteRecap(jobId: String) = recapDao.deleteRecap(jobId)

    fun buildPlaybackUrl(baseUrl: String, recap: RecapItem, quality: String): String {
        return apiClient.buildVideoUrl(
            baseUrl = baseUrl,
            jobId = recap.jobId,
            finalVideoUrl = recap.finalVideoUrl,
            quality = quality
        )
    }

    private fun RecapEntity.toDomain(): RecapItem {
        val parsedScenes = try {
            scenesAdapter.fromJson(scenesJson).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        return RecapItem(
            jobId = jobId,
            title = title,
            category = category,
            lengthMinutes = lengthMinutes,
            durationSec = durationSec,
            finalVideoUrl = finalVideoUrl,
            thumbnailUrl = thumbnailUrl,
            scenes = parsedScenes,
            isPublished = isPublished,
            isDraft = isDraft,
            isSaved = isSaved,
            isLiked = isLiked,
            lastWatchedAt = lastWatchedAt,
            createdAt = createdAt,
            viewCount = viewCount
        )
    }
}
