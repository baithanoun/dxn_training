package com.dxn.training.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePromptTest {
    private val url = "https://example.org/hikaya-tamayoz-1.1.0.apk"

    @Test
    fun newerPublishedBuildIsAnUpdate() = assertTrue(isUpdateAvailable(2, 3, url))

    @Test
    fun sameOrOlderBuildIsNotAnUpdate() {
        assertFalse(isUpdateAvailable(3, 3, url))
        assertFalse(isUpdateAvailable(3, 2, url))
    }

    @Test
    fun nothingPublishedOrNoLinkIsNotAnUpdate() {
        assertFalse(isUpdateAvailable(2, null, url))
        assertFalse(isUpdateAvailable(2, 3, null))
        assertFalse(isUpdateAvailable(2, 3, " "))
    }
}
