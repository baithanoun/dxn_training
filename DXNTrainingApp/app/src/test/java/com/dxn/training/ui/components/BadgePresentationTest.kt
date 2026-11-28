package com.dxn.training.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BadgePresentationTest {

    @Test
    fun mapsAllComputedBadgeIds() {
        assertEquals("أول قسم", badgePresentation("first_section")?.maleLabel)
        assertEquals("سلسلة ٧ أيام", badgePresentation("streak_7")?.femaleLabel)
        assertEquals("إكمال كامل", badgePresentation("all_complete")?.maleLabel)
    }

    @Test
    fun ignoresUnknownBadgeIds() {
        assertNull(badgePresentation("manual_award"))
    }
}
