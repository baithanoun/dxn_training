package com.dxn.training.data.repository

import com.dxn.training.data.model.TeamMember
import com.dxn.training.data.repository.MockData.teamMembers

class TeamRepository {
    fun getTeamMembers(): List<TeamMember> = teamMembers
}
