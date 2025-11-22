package com.example.insanecrossmobilepingpongapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.insanecrossmobilepingpongapp.sensor.AndroidMotionSensor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Pass the Android-specific sensor implementation with Context
            App(motionSensor = AndroidMotionSensor(this))
        }
    }
}