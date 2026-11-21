package com.dxn.training.data.model

data class UserProgress(
    val userId: String,
    val levelId: String,
    val status: LevelStatus,
    val score: Int = 0,
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val sectionCompleted: Boolean = false,
    val passedFinalQuiz: Boolean = false
)

data class SectionProgress(
    val userId: String,
    val sectionId: String,
    val status: SectionStatus,
    val certificateIssued: Boolean = false,
    val certificateId: String? = null,
    val completedAt: String? = null
)
