package com.dxn.training.data.repository

import android.util.Log
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.model.*
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.AnswerDto
import com.dxn.training.data.remote.LevelCompletionRequest
import com.dxn.training.data.remote.LevelCompletionResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import org.json.JSONObject

private const val TAG = "ContentRepository"

sealed class CompletionResult {
    data class Ok(val response: LevelCompletionResponse) : CompletionResult()
    data class Locked(val message: String) : CompletionResult()
    object Offline : CompletionResult()
    data class Failed(val message: String) : CompletionResult()
}

class ContentRepository private constructor(
    private val api: ApiService,
    private val sessionStore: SessionStore?,
    private val userRepository: UserRepository
) {
    companion object {
        @Volatile
        private var instance: ContentRepository? = null

        fun getInstance(
            api: ApiService? = null,
            sessionStore: SessionStore? = ApiClient.getSessionStore(),
            userRepository: UserRepository? = null
        ): ContentRepository {
            return instance ?: synchronized(this) {
                instance ?: ContentRepository(
                    api = api ?: ApiClient.create(),
                    sessionStore = sessionStore ?: ApiClient.getSessionStore(),
                    userRepository = userRepository ?: UserRepository()
                ).also { instance = it }
            }
        }
    }

    // ===================  الحالة الداخلية  ===================

    // المحتوى المحلي للعرض فقط عند تعذّر تحميل الشجرة من السيرفر.
    private val _sections = MutableStateFlow(MockData.sections)
    val sections: StateFlow<List<Section>> = _sections.asStateFlow()

    // التقدّم وحالات الأقسام مصدرهما السيرفر فقط؛ لا حساب محلي للفتح أو الشهادات.
    private val _progress = MutableStateFlow(emptyMap<String, UserProgress>())
    val progress: StateFlow<Map<String, UserProgress>> = _progress.asStateFlow()

    private val _sectionProgress = MutableStateFlow(emptyMap<String, SectionProgress>())
    val sectionProgress: StateFlow<Map<String, SectionProgress>> = _sectionProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _loadedFromNetwork = MutableStateFlow(false)
    val loadedFromNetwork: StateFlow<Boolean> = _loadedFromNetwork.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // ===================  قراءة المحتوى  ===================

    fun getSections(): List<Section> = _sections.value

    fun getLevel(levelId: String): Level? {
        return _sections.value.flatMap { it.skills }.flatMap { it.levels }.find { it.id == levelId }
    }

    fun findSectionForLevel(levelId: String): Section? {
        return _sections.value.find { it.skills.any { s -> s.levels.any { l -> l.id == levelId } } }
    }

    fun findSkillForLevel(levelId: String): Skill? {
        return findSectionForLevel(levelId)?.skills?.find { it.levels.any { l -> l.id == levelId } }
    }

    fun getProgress(levelId: String): UserProgress {
        return _progress.value[levelId]
            ?: UserProgress(_currentUserId.value, levelId, LevelStatus.LOCKED)
    }

    fun getSectionProgress(sectionId: String): SectionProgress {
        return _sectionProgress.value[sectionId]
            ?: SectionProgress(_currentUserId.value, sectionId, SectionStatus.LOCKED)
    }

    // ===================  تحميل البيانات من السيرفر  ===================

    /**
     * تحميل شجرة المحتوى وتقدّم المستخدم من السيرفر. عند فشل الشجرة تبقى البيانات
     * المحلية للعرض فقط، وتُعرض لافتة "وضع غير متصل" في الواجهة.
     * @return true إذا نجح تحميل الأقسام على الأقل.
     */
    suspend fun loadContent(userId: String): Boolean {
        _currentUserId.value = userId
        _isLoading.value = true
        _lastError.value = null
        _loadedFromNetwork.value = false

        var sectionsLoadedFromNetwork = false

        try {
            // 1) الأقسام
            val sectionsResponse = api.getSectionsTree()
            if (sectionsResponse.isSuccessful) {
                val body = sectionsResponse.body()
                if (body != null && body.isNotEmpty()) {
                    _sections.value = body.map { it.toDomain() }
                    sectionsLoadedFromNetwork = true
                } else {
                    Log.w(TAG, "getSectionsTree returned empty body; keeping local sections")
                }
            } else {
                logError("getSectionsTree", sectionsResponse.code())
            }

            // 2) تقدم المستويات — استبدال كامل بحالة السيرفر
            val progressResponse = api.getUserProgress(userId)
            if (progressResponse.isSuccessful) {
                val body = progressResponse.body()
                if (body != null) {
                    _progress.value = body.mapValues { (levelId, response) ->
                        response.toDomain(levelId, userId)
                    }
                }
            } else {
                logError("getUserProgress", progressResponse.code())
            }

            // 3) تقدم الأقسام والشهادات — استبدال كامل بحالة السيرفر
            val sectionProgressResponse = api.getSectionProgress(userId)
            if (sectionProgressResponse.isSuccessful) {
                val body = sectionProgressResponse.body()
                if (body != null) {
                    _sectionProgress.value = body.mapValues { (sectionId, response) ->
                        response.toDomain(sectionId, userId)
                    }
                }
            } else {
                logError("getSectionProgress", sectionProgressResponse.code())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Network error while loading content", e)
            _lastError.value = "تعذر الاتصال بالخادم. يُعرض المحتوى المحلي كاحتياط."
        } finally {
            _isLoading.value = false
        }

        _loadedFromNetwork.value = sectionsLoadedFromNetwork
        return sectionsLoadedFromNetwork
    }

    suspend fun refreshAll(userId: String): Boolean = loadContent(userId)

    // ===================  مزامنة السيرفر  ===================

    /**
     * إرسال إجابات المستوى إلى السيرفر. النتيجة والXP والشهادة كلها من الاستجابة؛
     * لا يُحدَّث أي تقدّم محلي قبل نجاح الطلب.
     */
    suspend fun submitLevelCompletion(
        levelId: String,
        answers: List<AnswerDto>
    ): CompletionResult {
        val userId = sessionStore?.session?.first()?.userId ?: return CompletionResult.Offline
        _currentUserId.value = userId
        return try {
            val response = api.completeLevel(levelId, LevelCompletionRequest(answers))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    userRepository.applyCompletionResult(body)
                    refreshAll(userId)
                    CompletionResult.Ok(body)
                } else {
                    CompletionResult.Failed("تعذر حفظ النتيجة")
                }
            } else {
                val raw = response.errorBody()?.string()
                when (response.code()) {
                    409 -> CompletionResult.Locked(errorMessage(raw) ?: "المستوى مقفل")
                    422 -> CompletionResult.Failed(errorMessage(raw) ?: "بيانات غير صالحة")
                    else -> {
                        logError("completeLevel", response.code())
                        CompletionResult.Failed("خطأ في الخادم (${response.code()})")
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Network error while completing level", e)
            CompletionResult.Offline
        }
    }

    // ===================  مساعدون  ===================

    private fun logError(endpoint: String, code: Int) {
        Log.w(TAG, "$endpoint failed with HTTP $code")
        _lastError.value = "خطأ في الخادم ($code). يُعرض المحتوى المحلي."
    }

    private fun errorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() } ?: raw
        } catch (e: Exception) {
            raw
        }
    }
}
