package com.dxn.training.data.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val dxnId: String,
    val role: UserRole,
    val gender: String? = null,
    val sponsorId: String? = null,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val hearts: Int = 3,
    val gems: Int = 0,
    val badges: List<Badge> = emptyList(),
    val certificates: List<Certificate> = emptyList()
)

enum class UserRole {
    TRAINEE, LEADER, ADMIN
}

data class Badge(
    val id: String,
    val name: String,
    val icon: String,
    val earnedAt: String
)

data class Certificate(
    val id: String,
    val title: String,
    val issuedAt: String,
    // Server section id, needed to open the matching certificate preview screen.
    val sectionId: String = ""
)
