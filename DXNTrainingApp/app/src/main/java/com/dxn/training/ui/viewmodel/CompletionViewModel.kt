package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.remote.AnswerDto
import com.dxn.training.data.repository.CompletionResult
import com.dxn.training.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompletionUiState(
    val levelId: String? = null,
    val answers: List<AnswerDto> = emptyList(),
    val result: CompletionResult? = null,
    val isSubmitting: Boolean = false
)

class CompletionViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CompletionUiState())
    val state: StateFlow<CompletionUiState> = _state.asStateFlow()

    fun submit(levelId: String, answers: List<AnswerDto>) {
        viewModelScope.launch {
            _state.value = CompletionUiState(
                levelId = levelId,
                answers = answers,
                isSubmitting = true
            )
            val result = contentRepository.submitLevelCompletion(levelId, answers)
            _state.update { it.copy(result = result, isSubmitting = false) }
        }
    }

    fun retry() {
        val current = _state.value
        current.levelId?.let { submit(it, current.answers) }
    }
}
