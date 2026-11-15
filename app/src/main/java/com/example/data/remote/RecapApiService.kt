package com.example.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RecapApiService {

    @Multipart
    @POST("api/jobs")
    suspend fun createJob(
        @Part video: MultipartBody.Part,
        @Part("lengthMinutes") lengthMinutes: RequestBody,
        @Part("category") category: RequestBody
    ): Response<CreateJobResponse>

    @GET("api/jobs")
    suspend fun listJobsRaw(): Response<ResponseBody>

    @GET("api/jobs/{jobId}")
    suspend fun getJobStatus(
        @Path("jobId") jobId: String
    ): Response<JobStatusResponse>

    @POST("api/jobs/{jobId}/script")
    suspend fun approveScript(
        @Path("jobId") jobId: String,
        @Body body: ApproveScriptRequest
    ): Response<ResponseBody>

    @POST("api/jobs/{jobId}/generate")
    suspend fun startGenerate(
        @Path("jobId") jobId: String
    ): Response<ResponseBody>
}
