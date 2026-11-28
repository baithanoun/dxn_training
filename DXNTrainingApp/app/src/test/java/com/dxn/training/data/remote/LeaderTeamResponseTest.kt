package com.dxn.training.data.remote

import com.dxn.training.data.model.TeamSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderTeamResponseTest {

    @Test
    fun mapsBadgesAndSectionSummaries() {
        val response = LeaderTeamMemberResponse(
            id = "7",
            name = "متدرب",
            badges = listOf("first_section", "streak_7"),
            sections = listOf(
                LeaderTeamSectionResponse(
                    title = "أساسيات DXN",
                    completedLevels = 3,
                    totalLevels = 4,
                    certificate = true
                )
            )
        )

        val member = response.toDomain()

        assertEquals(listOf("first_section", "streak_7"), member.badges)
        assertEquals(1, member.sections.size)
        assertEquals("أساسيات DXN", member.sections[0].title)
        assertEquals(3, member.sections[0].completedLevels)
        assertEquals(4, member.sections[0].totalLevels)
        assertTrue(member.sections[0].certificate)
    }

    @Test
    fun mapsEmptyBadgesAndSectionsByDefault() {
        val member = LeaderTeamMemberResponse(id = "8", name = "متدرب آخر").toDomain()

        assertEquals(emptyList<String>(), member.badges)
        assertEquals(emptyList<TeamSection>(), member.sections)
    }
}
