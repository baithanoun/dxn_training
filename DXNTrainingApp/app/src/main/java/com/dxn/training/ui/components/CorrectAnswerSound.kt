package com.dxn.training.ui.components

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays res/raw/correct.ogg when that asset has been supplied. A missing asset is a no-op,
 * so haptics and confetti still work and the app builds before the audio file arrives.
 */
object CorrectAnswerSound {
    fun play(context: Context) {
        val soundResId = context.resources.getIdentifier("correct", "raw", context.packageName)
        if (soundResId == 0) return
        runCatching {
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        }
    }
}
