package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.dxn.training.data.model.TeamMember
import com.dxn.training.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeamViewModel(
    private val teamRepository: TeamRepository = TeamRepository()
) : ViewModel() {
    private val _members = MutableStateFlow(teamRepository.getTeamMembers())
    val members: StateFlow<List<TeamMember>> = _members.asStateFlow()
}
