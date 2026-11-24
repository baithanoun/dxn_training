package com.dxn.training.data.remote

import com.dxn.training.data.model.*

// ===================  نماذج JSON القادمة من السيرفر  ===================

data class AppConfigResponse(
    val appName: String,
    val teamName: String,
    val teamTagline: String?,
    val teamBio: String?,
    val teamWelcome: String?,
    val maintenanceMode: Boolean
)

/**
 * نموذج القسم القادم من السيرفر.
 */
data class SectionResponse(
    val id: String,
    val title: String,
    val orderIndex: Int,
    val bagId: Int? = null,
    val description: String = "",
    val certificateTitle: String = "",
    val requiredSectionId: String? = null,
    val skills: List<SkillResponse> = emptyList()
) {
    fun toDomain(): Section = Section(
        id = id,
        title = title,
        orderIndex = orderIndex,
        bagId = bagId,
        description = description,
        certificateTitle = certificateTitle,
        requiredSectionId = requiredSectionId,
        skills = skills.map { it.toDomain(id) }
    )
}

data class SkillResponse(
    val id: String,
    val title: String,
    val orderIndex: Int,
    val levels: List<LevelResponse> = emptyList()
) {
    fun toDomain(sectionId: String): Skill = Skill(
        id = id,
        sectionId = sectionId,
        title = title,
        orderIndex = orderIndex,
        levels = levels.map { it.toDomain(id) }
    )
}

data class LevelResponse(
    val id: String,
    val title: String,
    val orderIndex: Int,
    val xpReward: Int,
    val passThreshold: Int,
    val type: String = "lesson",
    val cards: List<LessonCardResponse> = emptyList(),
    val rounds: List<GameRoundResponse> = emptyList()
) {
    fun toDomain(skillId: String): Level = Level(
        id = id,
        skillId = skillId,
        title = title,
        orderIndex = orderIndex,
        xpReward = xpReward,
        passThreshold = passThreshold,
        type = type,
        cards = cards.map { it.toDomain() },
        rounds = rounds.map { it.toDomain() }
    )
}

data class LessonCardResponse(
    val id: String,
    val title: String,
    val body: String,
    val tip: String? = null
) {
    fun toDomain(): LessonCard = LessonCard(
        id = id,
        title = title,
        body = body,
        tip = tip
    )
}

data class GameRoundResponse(
    val id: String,
    val prompt: String,
    val initialTrust: Int,
    val options: List<RoundOptionResponse> = emptyList()
) {
    fun toDomain(): GameRound = GameRound(
        id = id,
        prompt = prompt,
        initialTrust = initialTrust,
        options = options.map { it.toDomain() }
    )
}

data class RoundOptionResponse(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val isNeutral: Boolean = false,
    val trustImpact: Int,
    val feedback: String,
    val xpBonus: Int = 0
) {
    fun toDomain(): RoundOption = RoundOption(
        id = id,
        text = text,
        isCorrect = isCorrect,
        isNeutral = isNeutral,
        trustImpact = trustImpact,
        feedback = feedback,
        xpBonus = xpBonus
    )
}

// ===================  نماذج تقدم المستخدم  ===================

data class UserProgressResponse(
    val status: String = "LOCKED",
    val score: Int = 0,
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val sectionCompleted: Boolean = false,
    val passedFinalQuiz: Boolean = false
) {
    fun toDomain(levelId: String, userId: String): UserProgress = UserProgress(
        userId = userId,
        levelId = levelId,
        status = status.toLevelStatus(),
        score = score,
        attempts = attempts,
        bestScore = bestScore,
        sectionCompleted = sectionCompleted,
        passedFinalQuiz = passedFinalQuiz
    )
}

data class SectionProgressResponse(
    val status: String = "LOCKED",
    val certificateIssued: Boolean = false,
    val certificateId: String? = null,
    val completedAt: String? = null
) {
    fun toDomain(sectionId: String, userId: String): SectionProgress = SectionProgress(
        userId = userId,
        sectionId = sectionId,
        status = status.toSectionStatus(),
        certificateIssued = certificateIssued,
        certificateId = certificateId,
        completedAt = completedAt
    )
}

private fun String.toLevelStatus(): LevelStatus = try {
    LevelStatus.valueOf(this.uppercase())
} catch (e: Exception) {
    LevelStatus.LOCKED
}

private fun String.toSectionStatus(): SectionStatus = try {
    SectionStatus.valueOf(this.uppercase())
} catch (e: Exception) {
    SectionStatus.LOCKED
}
