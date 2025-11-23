package com.example.insanecrossmobilepingpongapp.util

import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class SoundPlayer {
    private var audioPlayer: AVAudioPlayer? = null

    init {
        val soundUrl = NSBundle.mainBundle.URLForResource("swing", "mp3")
        if (soundUrl != null) {
            audioPlayer = AVAudioPlayer(contentsOfURL = soundUrl, error = null)
            audioPlayer?.prepareToPlay()
        }
    }

    actual fun playSwingSound() {
        audioPlayer?.currentTime = 0.0
        audioPlayer?.play()
    }
}
