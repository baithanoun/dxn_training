package com.dxn.training.data.repository

import com.dxn.training.data.remote.UserProfileResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileBadgesTest {

    @Test
    fun mapsComputedBadgesFromProfileResponse() {
        val profile = UserProfileResponse(
            id = "9",
            fullName = "متدرب",
            dxnId = "DXN-9",
            role = "TRAINEE",
            badges = listOf("first_section", "streak_7", "all_complete")
        )

        assertEquals(
            listOf("first_section", "streak_7", "all_complete"),
            profile.toDomain().badges
        )
    }

    @Test
    fun mapsEmptyBadgesByDefault() {
        val profile = UserProfileResponse(
            id = "10",
            fullName = "متدرب",
            dxnId = "DXN-10",
            role = "TRAINEE"
        )

        assertEquals(emptyList<String>(), profile.toDomain().badges)
    }
}
