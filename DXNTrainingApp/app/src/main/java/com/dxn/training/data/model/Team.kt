package com.dxn.training.data.model

data class TeamMember(
    val id: String,
    val name: String,
    val progressPercent: Int,
    val lastActiveDays: Int,
    val status: MemberStatus
)

enum class MemberStatus {
    ACTIVE, INACTIVE, COMPLETED
}
