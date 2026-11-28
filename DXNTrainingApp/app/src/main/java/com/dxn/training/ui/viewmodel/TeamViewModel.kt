package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.model.TeamMember
import com.dxn.training.data.model.UserProgress
import com.dxn.training.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val teamRepository: TeamRepository = TeamRepository()
) : ViewModel() {
    private val _members = MutableStateFlow<List<TeamMember>>(emptyList())
    val members: StateFlow<List<TeamMember>> = _members.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _grades = MutableStateFlow<Map<String, UserProgress>>(emptyMap())
    val grades: StateFlow<Map<String, UserProgress>> = _grades.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = teamRepository.load()
                result.onSuccess { _members.value = it }
                result.onFailure {
                    _members.value = emptyList()
                    _error.value = "تعذر تحميل الفريق"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGrades(memberId: String) {
        viewModelScope.launch {
            _grades.value = emptyMap()
            teamRepository.loadGrades(memberId)
                .onSuccess { _grades.value = it }
                .onFailure { _grades.value = emptyMap() }
        }
    }
}
