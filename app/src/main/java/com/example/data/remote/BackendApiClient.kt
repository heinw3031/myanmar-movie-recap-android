package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class BackendApiClient {

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    private var cachedBaseUrl: String? = null
    private var cachedService: RecapApiService? = null

    @Synchronized
    fun getService(rawBaseUrl: String): RecapApiService? {
        val normalized = normalizeBaseUrl(rawBaseUrl) ?: return null
        val retrofitUrl = "$normalized/"
        if (cachedBaseUrl == retrofitUrl && cachedService != null) {
            return cachedService
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(retrofitUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val service = retrofit.create(RecapApiService::class.java)
        cachedBaseUrl = retrofitUrl
        cachedService = service
        return service
    }

    fun normalizeBaseUrl(rawBaseUrl: String): String? {
        val trimmed = rawBaseUrl.trim().trimEnd('/')
        if (trimmed.isBlank()) return null
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun resolveUrl(baseUrl: String, pathOrUrl: String): String {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl
        }
        val base = normalizeBaseUrl(baseUrl) ?: return pathOrUrl
        val cleanPath = if (pathOrUrl.startsWith("/")) pathOrUrl else "/$pathOrUrl"
        return "$base$cleanPath"
    }

    fun buildVideoUrl(baseUrl: String, jobId: String, finalVideoUrl: String?, quality: String): String {
        val rawUrl = if (!finalVideoUrl.isNullOrBlank()) {
            resolveUrl(baseUrl, finalVideoUrl)
        } else {
            resolveUrl(baseUrl, "/api/jobs/$jobId/video")
        }
        if (quality == "480") {
            return if (rawUrl.contains("quality=")) {
                rawUrl.replace(Regex("quality=[^&]+"), "quality=480")
            } else if (rawUrl.contains("?")) {
                "$rawUrl&quality=480"
            } else {
                "$rawUrl?quality=480"
            }
        }
        return rawUrl
    }

    fun extractErrorMessage(response: Response<*>, fallback: String): String {
        val raw = try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        if (!raw.isNullOrBlank()) {
            try {
                val adapter = moshi.adapter(ErrorBodyDto::class.java)
                val parsed = adapter.fromJson(raw)
                val msg = parsed?.error ?: parsed?.message ?: parsed?.detail
                if (!msg.isNullOrBlank()) {
                    return "HTTP ${response.code()}: $msg"
                }
            } catch (_: Exception) {
                return "HTTP ${response.code()}: ${raw.take(300)}"
            }
        }
        return "HTTP ${response.code()}: $fallback"
    }

    fun parseJobListJson(json: String): List<JobStatusResponse> {
        val trimmed = json.trim()
        if (trimmed.startsWith("[")) {
            val listType = Types.newParameterizedType(List::class.java, JobStatusResponse::class.java)
            val adapter = moshi.adapter<List<JobStatusResponse>>(listType)
            return adapter.fromJson(trimmed).orEmpty()
        }
        if (trimmed.startsWith("{")) {
            val wrapperAdapter = moshi.adapter(JobListWrapper::class.java)
            val wrapper = wrapperAdapter.fromJson(trimmed)
            return wrapper?.jobs ?: wrapper?.data ?: emptyList()
        }
        return emptyList()
    }
}
