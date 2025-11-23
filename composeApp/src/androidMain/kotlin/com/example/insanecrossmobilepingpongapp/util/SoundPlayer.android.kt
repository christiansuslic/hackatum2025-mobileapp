package com.example.insanecrossmobilepingpongapp.util

import android.media.MediaPlayer
import com.example.insanecrossmobilepingpongapp.R
import com.example.insanecrossmobilepingpongapp.util.ContextProvider

actual class SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    init {
        try {
            val context = ContextProvider.getContext()
            mediaPlayer = MediaPlayer.create(context, R.raw.swing)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun playSwingSound() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.seekTo(0)
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
