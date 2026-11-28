package com.dxn.training.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebrationTest {

    @Test
    fun reducedMotionDisablesConfetti() = assertFalse(confettiEnabled(0f))

    @Test
    fun normalMotionEnablesConfetti() {
        assertTrue(confettiEnabled(1f))
        assertTrue(confettiEnabled(0.5f))
    }
}
