package com.dxn.training.data.model

enum class SectionStatus {
    LOCKED, UNLOCKED, COMPLETED
}

data class Section(
    val id: String,
    val title: String,
    val orderIndex: Int,
    val bagId: Int? = null,           // الحقيبة المصدر (1..5)
    val description: String = "",
    val certificateTitle: String = "",
    val requiredSectionId: String? = null,
    val skills: List<Skill>
)

data class Skill(
    val id: String,
    val sectionId: String,
    val title: String,
    val orderIndex: Int,
    val levels: List<Level>
)

enum class LevelStatus {
    LOCKED, UNLOCKED, COMPLETED
}

data class Level(
    val id: String,
    val skillId: String,
    val title: String,
    val orderIndex: Int,
    val xpReward: Int,
    val passThreshold: Int,
    val type: String = "quiz",
    val cards: List<LessonCard>,
    val rounds: List<GameRound>
)

data class LessonCard(
    val id: String,
    val title: String,
    val body: String,
    val tip: String? = null
)

data class GameRound(
    val id: String,
    val prompt: String,
    val initialTrust: Int,
    val options: List<RoundOption>
)

data class RoundOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val isNeutral: Boolean = false,
    val trustImpact: Int,
    val feedback: String,
    val xpBonus: Int = 0
)
