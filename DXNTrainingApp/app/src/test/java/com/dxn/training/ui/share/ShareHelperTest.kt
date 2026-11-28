package com.dxn.training.ui.share

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareHelperTest {

    @Test
    fun buildAchievementTextIncludesRequiredParts() {
        val text = ShareHelper.buildAchievementText(
            prefix = "أكملتُ",
            levelName = "مستوى 1",
            scorePercent = 90,
            appDownloadUrl = "https://example.org/app.apk"
        )

        assertEquals(
            "أكملتُ مستوى 1\nالدرجة: 90%\nمع فريق حكاية تميّز\nhttps://example.org/app.apk",
            text
        )
    }

    @Test
    fun buildAchievementTextOmitsBlankOptionalParts() {
        val text = ShareHelper.buildAchievementText(
            prefix = "أكملتُ",
            levelName = "",
            scorePercent = null,
            appDownloadUrl = " "
        )

        assertEquals("أكملتُ\nمع فريق حكاية تميّز", text)
    }
}
