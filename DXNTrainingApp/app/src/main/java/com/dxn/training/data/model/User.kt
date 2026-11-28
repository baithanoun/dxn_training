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
    // Computed badge ids from the server; no local award state (D5 option أ).
    val badges: List<String> = emptyList(),
    val certificates: List<Certificate> = emptyList()
)

enum class UserRole {
    TRAINEE, LEADER, ADMIN
}

data class Certificate(
    val id: String,
    val title: String,
    val issuedAt: String,
    // Server section id, needed to open the matching certificate preview screen.
    val sectionId: String = ""
)
