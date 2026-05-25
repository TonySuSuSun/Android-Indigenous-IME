package com.litekite.ime.audio

import android.content.Context
import android.media.AudioManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioController @Inject constructor(context: Context) {

    private var audioController: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playSoundEffect() {
        audioController.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
    }
}
