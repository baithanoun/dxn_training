package com.dxn.training.data.repository

import android.util.Log
import com.dxn.training.data.model.*
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.LevelCompletionRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

private const val TAG = "ContentRepository"
private const val DEFAULT_USER_ID = "u1"

class ContentRepository(
    private val api: ApiService? = ApiClient.create(),
    private val syncScope: kotlinx.coroutines.CoroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
) {
    companion object {
        @Volatile
        private var instance: ContentRepository? = null

        fun getInstance(api: ApiService? = null): ContentRepository {
            return instance ?: synchronized(this) {
                instance ?: ContentRepository(api ?: ApiClient.create()).also { instance = it }
            }
        }
    }

    // ===================  الحالة الداخلية  ===================

    private val _sections = MutableStateFlow(MockData.sections)
    val sections: StateFlow<List<Section>> = _sections.asStateFlow()

    private val _progress = MutableStateFlow(MockData.userProgress.toMap())
    val progress: StateFlow<Map<String, UserProgress>> = _progress.asStateFlow()

    private val _sectionProgress = MutableStateFlow(MockData.sectionProgress.toMap())
    val sectionProgress: StateFlow<Map<String, SectionProgress>> = _sectionProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _loadedFromNetwork = MutableStateFlow(false)
    val loadedFromNetwork: StateFlow<Boolean> = _loadedFromNetwork.asStateFlow()

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
        return _progress.value[levelId] ?: UserProgress(DEFAULT_USER_ID, levelId, LevelStatus.LOCKED)
    }

    fun getSectionProgress(sectionId: String): SectionProgress {
        return _sectionProgress.value[sectionId]
            ?: SectionProgress(DEFAULT_USER_ID, sectionId, SectionStatus.LOCKED)
    }

    // ===================  تحميل البيانات من السيرفر  ===================

    /**
     * تحميل كل البيانات من السيرفر مع العودة إلى البيانات المحلية عند الفشل.
     * @return true إذا نجح تحميل الأقسام على الأقل.
     */
    suspend fun loadContent(userId: String = DEFAULT_USER_ID): Boolean {
        _isLoading.value = true
        _lastError.value = null
        _loadedFromNetwork.value = false

        var sectionsLoadedFromNetwork = false

        try {
            api?.let { service ->
                // 1) الأقسام
                val sectionsResponse = service.getSectionsTree()
                if (sectionsResponse.isSuccessful) {
                    val body = sectionsResponse.body()
                    if (body != null && body.isNotEmpty()) {
                        _sections.update { body.map { it.toDomain() } }
                        sectionsLoadedFromNetwork = true
                    } else {
                        Log.w(TAG, "getSectionsTree returned empty body; keeping local sections")
                    }
                } else {
                    logError("getSectionsTree", sectionsResponse.code())
                }

                // 2) تقدم المستويات — دمج مع البيانات المحلية بدلاً من الاستبدال
                val progressResponse = service.getUserProgress(userId)
                if (progressResponse.isSuccessful) {
                    val body = progressResponse.body()
                    if (body != null && body.isNotEmpty()) {
                        val serverProgress = body.mapValues { (levelId, response) ->
                            response.toDomain(levelId, userId)
                        }
                        _progress.update { local ->
                            val merged = local.toMutableMap()
                            serverProgress.forEach { (levelId, serverValue) ->
                                val localValue = local[levelId]
                                // الاحتفاظ بالتقدم المحلي إذا كان أعلى (تلافي race)
                                if (localValue == null || localValue.attempts <= serverValue.attempts) {
                                    merged[levelId] = serverValue
                                }
                            }
                            merged
                        }
                    }
                } else {
                    logError("getUserProgress", progressResponse.code())
                }

                // 3) تقدم الأقسام والشهادات — دمج مع البيانات المحلية
                val sectionProgressResponse = service.getSectionProgress(userId)
                if (sectionProgressResponse.isSuccessful) {
                    val body = sectionProgressResponse.body()
                    if (body != null && body.isNotEmpty()) {
                        val serverProgress = body.mapValues { (sectionId, response) ->
                            response.toDomain(sectionId, userId)
                        }
                        _sectionProgress.update { local ->
                            val merged = local.toMutableMap()
                            serverProgress.forEach { (sectionId, serverValue) ->
                                val localValue = local[sectionId]
                                // الاحتفاظ بالتقدم المحلي إذا كان highest status
                                if (localValue == null ||
                                    localValue.status.ordinal <= serverValue.status.ordinal) {
                                    merged[sectionId] = serverValue
                                }
                            }
                            merged
                        }
                    }
                } else {
                    logError("getSectionProgress", sectionProgressResponse.code())
                }
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

    suspend fun refreshAll(userId: String = DEFAULT_USER_ID): Boolean = loadContent(userId)

    // ===================  منطق القفل والشهادات  ===================

    /**
     * القسم مفتوح إذا:
     * - لا يتطلب قسماً سابقاً، أو
     * - القسم السابق مكتمل (COMPLETED) وتم إصدار شهادته.
     */
    private fun canSectionBeUnlocked(sectionId: String): Boolean {
        val section = _sections.value.find { it.id == sectionId } ?: return false
        val requiredId = section.requiredSectionId ?: return true
        val requiredProgress = getSectionProgress(requiredId)
        return requiredProgress.status == SectionStatus.COMPLETED && requiredProgress.certificateIssued
    }

    private fun computeSectionStatus(sectionId: String): SectionStatus {
        if (!canSectionBeUnlocked(sectionId)) return SectionStatus.LOCKED

        val section = _sections.value.find { it.id == sectionId } ?: return SectionStatus.LOCKED
        val allLevels = section.skills.flatMap { it.levels }
        if (allLevels.isEmpty()) return SectionStatus.LOCKED

        val progressMap = _progress.value
        val allCompleted = allLevels.all { progressMap[it.id]?.status == LevelStatus.COMPLETED }
        val hasUnlocked = allLevels.any { progressMap[it.id]?.status != LevelStatus.LOCKED }

        return when {
            allCompleted -> SectionStatus.COMPLETED
            hasUnlocked -> SectionStatus.UNLOCKED
            else -> SectionStatus.LOCKED
        }
    }

    /**
     * إصدار شهادة القسم فقط بعد اجتياز اختبار إتمام القسم (final quiz).
     * آخر مستوى في آخر مهارة في القسم هو اختبار الإتمام.
     */
    private fun hasPassedFinalQuiz(sectionId: String): Boolean {
        val section = _sections.value.find { it.id == sectionId } ?: return false
        val finalLevel = section.skills.lastOrNull()?.levels?.lastOrNull() ?: return false
        val finalProgress = _progress.value[finalLevel.id]
        return finalProgress?.status == LevelStatus.COMPLETED && finalProgress.passedFinalQuiz
    }

    private fun isFinalLevelOfSection(levelId: String): Boolean {
        val section = findSectionForLevel(levelId) ?: return false
        val finalLevel = section.skills.lastOrNull()?.levels?.lastOrNull()
        return finalLevel?.id == levelId
    }

    private fun issueCertificateIfNeeded(sectionId: String) {
        val section = _sections.value.find { it.id == sectionId } ?: return
        if (section.certificateTitle.isBlank()) return

        val current = getSectionProgress(sectionId)
        if (current.status == SectionStatus.COMPLETED && !current.certificateIssued && hasPassedFinalQuiz(sectionId)) {
            _sectionProgress.update { it.toMutableMap().apply {
                put(
                    sectionId,
                    current.copy(
                        certificateIssued = true,
                        certificateId = "cert-${sectionId}",
                        completedAt = "2026-11-17"
                    )
                )
            } }
        }
    }

    private fun recomputeAllSections() {
        _sections.value.forEach { section ->
            val newStatus = computeSectionStatus(section.id)
            val current = getSectionProgress(section.id)

            // لا نسمح بتحويل قسم مكتمل إلى غير مكتمل
            val effectiveStatus = if (current.status == SectionStatus.COMPLETED) SectionStatus.COMPLETED else newStatus

            if (current.status != effectiveStatus) {
                _sectionProgress.update { it.toMutableMap().apply {
                    put(section.id, current.copy(status = effectiveStatus))
                } }
            }

            if (effectiveStatus == SectionStatus.COMPLETED) {
                issueCertificateIfNeeded(section.id)
            }
        }
    }

    // ===================  تحديث التقدم  ===================

    fun updateLevelProgress(levelId: String, score: Int, passed: Boolean) {
        val current = getProgress(levelId)
        val currentSection = findSectionForLevel(levelId)

        // لا يمكن إكمال مستوى في قسم مقفل
        if (currentSection != null && computeSectionStatus(currentSection.id) == SectionStatus.LOCKED) {
            return
        }

        val isFinalQuiz = isFinalLevelOfSection(levelId)
        val newStatus = if (passed) LevelStatus.COMPLETED else current.status
        val updated = current.copy(
            status = if (current.status == LevelStatus.LOCKED && passed) LevelStatus.UNLOCKED else newStatus,
            score = score,
            attempts = current.attempts + 1,
            bestScore = maxOf(current.bestScore, score),
            passedFinalQuiz = if (isFinalQuiz && passed) true else current.passedFinalQuiz
        )
        _progress.update { it.toMutableMap().apply { put(levelId, updated) } }

        if (passed) {
            // فتح المستوى التالي داخل نفس المهارة
            val skill = findSkillForLevel(levelId)
            val levelsInSkill = skill?.levels ?: emptyList()
            val currentIndex = levelsInSkill.indexOfFirst { it.id == levelId }
            val nextInSkill = levelsInSkill.getOrNull(currentIndex + 1)
            if (nextInSkill != null) {
                val nextProgress = getProgress(nextInSkill.id)
                if (nextProgress.status == LevelStatus.LOCKED) {
                    _progress.update { it.toMutableMap().apply {
                        put(nextInSkill.id, nextProgress.copy(status = LevelStatus.UNLOCKED))
                    } }
                }
            }

            // فتح المهارة التالية في نفس القسم إن وجدت
            val section = currentSection
            if (section != null) {
                val skillIndex = section.skills.indexOfFirst { it.id == skill?.id }
                val nextSkill = section.skills.getOrNull(skillIndex + 1)
                val firstLevelOfNextSkill = nextSkill?.levels?.firstOrNull()
                if (firstLevelOfNextSkill != null) {
                    val nextProgress = getProgress(firstLevelOfNextSkill.id)
                    if (nextProgress.status == LevelStatus.LOCKED) {
                        _progress.update { it.toMutableMap().apply {
                            put(firstLevelOfNextSkill.id, nextProgress.copy(status = LevelStatus.UNLOCKED))
                        } }
                    }
                }
            }

            // إعادة حساب حالات الأقسام وإصدار الشهادات
            recomputeAllSections()

            // مزامنة مع السيرفر (best-effort، لا نمنع UI)
            syncScope.launch {
                submitLevelCompletion(levelId, score, passed)
            }
        }
    }

    /**
     * يُستخدم للسماح للمستخدم بفتح مستوى مقفل عندما يكون القسم متاحاً
     * (مثلاً عند الضغط على "ابدأ" لأول مرة).
     */
    fun unlockFirstLevelOfSection(sectionId: String): Boolean {
        if (computeSectionStatus(sectionId) == SectionStatus.LOCKED) return false
        val section = _sections.value.find { it.id == sectionId } ?: return false
        val firstLevel = section.skills.firstOrNull()?.levels?.firstOrNull() ?: return false
        val progress = getProgress(firstLevel.id)
        if (progress.status == LevelStatus.LOCKED) {
            _progress.update { it.toMutableMap().apply {
                put(firstLevel.id, progress.copy(status = LevelStatus.UNLOCKED))
            } }
        }
        return true
    }

    // ===================  مزامنة السيرفر  ===================

    /**
     * مزامنة تقديم إكمال مستوى إلى السيرفر. تُستدعى صراحةً من ViewModel.
     */
    suspend fun submitLevelCompletion(
        levelId: String,
        score: Int,
        passed: Boolean,
        userId: String = DEFAULT_USER_ID
    ): Boolean {
        val service = api ?: return false
        return try {
            val response = service.completeLevel(
                levelId,
                LevelCompletionRequest(userId, score, passed)
            )
            if (!response.isSuccessful) {
                logError("completeLevel", response.code())
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Network error while completing level", e)
            false
        }
    }

    // ===================  مساعدون  ===================

    private fun logError(endpoint: String, code: Int) {
        Log.w(TAG, "$endpoint failed with HTTP $code")
        _lastError.value = "خطأ في الخادم ($code). يُعرض المحتوى المحلي."
    }
}
