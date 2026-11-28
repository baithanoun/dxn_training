package com.dxn.training.data.model

data class TeamMember(
    val id: String,
    val name: String,
    val progressPercent: Int,
    val lastActiveDays: Int,
    val status: MemberStatus,
    // Computed badge ids and section summaries come from /api/leader/team.
    val badges: List<String> = emptyList(),
    val sections: List<TeamSection> = emptyList()
)

data class TeamSection(
    val title: String,
    val completedLevels: Int,
    val totalLevels: Int,
    val certificate: Boolean
)

enum class MemberStatus {
    ACTIVE, INACTIVE, COMPLETED
}
