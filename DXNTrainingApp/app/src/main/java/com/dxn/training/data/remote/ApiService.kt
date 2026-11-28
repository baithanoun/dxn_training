package com.dxn.training.data.remote

import com.dxn.training.data.model.MemberStatus
import com.dxn.training.data.model.TeamMember
import com.dxn.training.data.model.UserProgress
import com.dxn.training.data.model.SectionProgress
import com.dxn.training.data.model.TeamSection
import retrofit2.Response
import retrofit2.http.*

// ===================  الطلبات والاستجابات  ===================

data class AnswerDto(
    val roundId: String,
    val optionId: String? = null,
    val orderedOptionIds: List<String>? = null
)

data class LevelCompletionRequest(
    val answers: List<AnswerDto>
)

data class LevelCompletionResponse(
    val success: Boolean,
    val score: Int = 0,
    val passed: Boolean = false,
    val xpAwarded: Int = 0,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val certificateIssued: Boolean = false,
    val certificateTitle: String? = null,
    val certificateId: String? = null
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class DeleteAccountRequest(
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String,
    val dxnId: String,
    val gender: String,
    val leaderDxnId: String? = null,
    val acceptTerms: Boolean = false
)

data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
)

// ===================  الفريق والأحداث  ===================

data class LeaderTeamMemberResponse(
    val id: String,
    val name: String,
    val progressPercent: Int = 0,
    val lastActiveDays: Int = 0,
    val status: String = "INACTIVE",
    val badges: List<String> = emptyList(),
    val sections: List<LeaderTeamSectionResponse> = emptyList()
) {
    fun toDomain(): TeamMember = TeamMember(
        id = id,
        name = name,
        progressPercent = progressPercent,
        lastActiveDays = lastActiveDays,
        status = try {
            MemberStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            MemberStatus.INACTIVE
        },
        badges = badges,
        sections = sections.map { it.toDomain() }
    )
}

data class LeaderTeamSectionResponse(
    val title: String = "",
    val completedLevels: Int = 0,
    val totalLevels: Int = 0,
    val certificate: Boolean = false
) {
    fun toDomain(): TeamSection = TeamSection(
        title = title,
        completedLevels = completedLevels,
        totalLevels = totalLevels,
        certificate = certificate
    )
}

data class AnalyticsEventRequest(
    val eventName: String,
    val levelId: String? = null,
    val payload: Map<String, Any?>? = null
)

data class AnalyticsEventsBatchRequest(
    val events: List<AnalyticsEventRequest>
)

data class AnalyticsEventsAcceptedResponse(
    val accepted: Int = 0
)

data class UserProfileResponse(
    val id: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val dxnId: String,
    val role: String,
    val gender: String? = null,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val hearts: Int = 3,
    val gems: Int = 0,
    val badges: List<String> = emptyList()
)

// ===================  API Interface  ===================

interface ApiService {

    // ===================  المصادقة  ===================

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @HTTP(method = "DELETE", path = "api/account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Response<Unit>

    // ===================  إعدادات التطبيق العامة  ===================

    @GET("api/app/config")
    suspend fun getAppConfig(): Response<AppConfigResponse>

    // ===================  المحتوى  ===================

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

    /**
     * أعضاء فريق القائد (للقادة فقط).
     */
    @GET("api/leader/team")
    suspend fun getLeaderTeam(): Response<List<LeaderTeamMemberResponse>>

    /**
     * إرسال دفعة أحداث من العميل. الأسماء المسموحة فقط هي التي تُرسَل.
     */
    @POST("api/events")
    suspend fun sendEvents(
        @Body request: AnalyticsEventsBatchRequest
    ): Response<AnalyticsEventsAcceptedResponse>
}
