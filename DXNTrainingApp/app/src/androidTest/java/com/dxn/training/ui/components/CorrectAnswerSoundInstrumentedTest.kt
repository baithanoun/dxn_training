package com.dxn.training.ui.components

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CorrectAnswerSoundInstrumentedTest {
    @Test
    fun missingSoundAssetIsHarmless() {
        // Until res/raw/correct.ogg is supplied, play() must be a silent no-op.
        CorrectAnswerSound.play(ApplicationProvider.getApplicationContext())
    }
}
