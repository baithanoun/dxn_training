package com.dxn.training.data.remote

import com.dxn.training.data.model.UserProgress
import com.dxn.training.data.model.SectionProgress
import retrofit2.Response
import retrofit2.http.*

// ===================  الطلبات والاستجابات  ===================

data class LevelCompletionRequest(
    val userId: String,
    val score: Int,
    val passed: Boolean
)

data class LevelCompletionResponse(
    val success: Boolean,
    val xpAwarded: Int = 0,
    val certificateIssued: Boolean = false,
    val certificateTitle: String? = null
)

data class UserProfileResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val dxnId: String,
    val role: String,
    val totalXp: Int,
    val currentStreak: Int,
    val hearts: Int,
    val gems: Int
)

// ===================  API Interface  ===================

interface ApiService {

    /**
     * جلب شجرة المحتوى الكاملة (الأقسام والمهارات والمستويات).
     */
    @GET("api/sections/tree")
    suspend fun getSectionsTree(): Response<List<SectionResponse>>

    /**
     * جلب تقدم المستخدم في المستويات.
     */
    @GET("api/users/{userId}/progress")
    suspend fun getUserProgress(
        @Path("userId") userId: String
    ): Response<Map<String, UserProgressResponse>>

    /**
     * جلب تقدم المستخدم في الأقسام والشهادات.
     */
    @GET("api/users/{userId}/sections-progress")
    suspend fun getSectionProgress(
        @Path("userId") userId: String
    ): Response<Map<String, SectionProgressResponse>>

    /**
     * تسجيل إكمال مستوى.
     */
    @POST("api/levels/{levelId}/complete")
    suspend fun completeLevel(
        @Path("levelId") levelId: String,
        @Body request: LevelCompletionRequest
    ): Response<LevelCompletionResponse>

    /**
     * جلب بيانات المستخدم.
     */
    @GET("api/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<UserProfileResponse>
}
