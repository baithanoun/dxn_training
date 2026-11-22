package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.model.Section
import com.dxn.training.data.model.SectionProgress
import com.dxn.training.data.model.UserProgress
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val contentRepository: ContentRepository = ContentRepository.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
    private val sessionStore: SessionStore? = ApiClient.getSessionStore()
) : ViewModel() {

    data class HomeUiState(
        val userName: String = "",
        val totalXp: Int = 0,
        val streak: Int = 0,
        val dailyProgress: Float = 0.55f,
        val sections: List<Section> = emptyList(),
        val progress: Map<String, UserProgress> = emptyMap(),
        val sectionProgress: Map<String, SectionProgress> = emptyMap(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.user,
        contentRepository.progress,
        contentRepository.sectionProgress,
        contentRepository.sections,
        _isLoading,
        _error
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val user = values[0] as com.dxn.training.data.model.User
        @Suppress("UNCHECKED_CAST")
        val progress = values[1] as Map<String, UserProgress>
        @Suppress("UNCHECKED_CAST")
        val sectionProgress = values[2] as Map<String, SectionProgress>
        @Suppress("UNCHECKED_CAST")
        val sections = values[3] as List<com.dxn.training.data.model.Section>
        val isLoading = values[4] as Boolean
        val error = values[5] as String?
        HomeUiState(
            userName = user.fullName.substringBefore(" "),
            totalXp = user.totalXp,
            streak = user.currentStreak,
            dailyProgress = 0.55f,
            sections = sections,
            progress = progress,
            sectionProgress = sectionProgress,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            val userId = sessionStore?.session?.first()?.userId
            if (userId == null) {
                _error.value = "انتهت الجلسة — سجّل الدخول من جديد"
                return@launch
            }
            _isLoading.value = true
            _error.value = null
            try {
                val success = contentRepository.loadContent(userId)
                userRepository.loadUserProfile(userId)
                if (!success) {
                    _error.value = "تعذر تحميل المحتوى من الخادم. يُعرض المحتوى المحلي كاحتياط."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "حدث خطأ أثناء تحميل المحتوى: ${e.localizedMessage ?: "خطأ غير معروف"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadContent()
    }

    fun clearError() {
        _error.value = null
    }

    fun unlockNextLevel(levelId: String) {
        viewModelScope.launch {
            contentRepository.updateLevelProgress(levelId, 0, true)
        }
    }

    fun unlockFirstLevelOfSection(sectionId: String): Boolean {
        return contentRepository.unlockFirstLevelOfSection(sectionId)
    }
}
