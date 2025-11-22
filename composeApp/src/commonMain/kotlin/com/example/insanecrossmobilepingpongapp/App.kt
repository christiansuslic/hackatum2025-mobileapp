package com.example.insanecrossmobilepingpongapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.insanecrossmobilepingpongapp.ui.ControllerScreen
import com.example.insanecrossmobilepingpongapp.controller.ControllerViewModel
import com.example.insanecrossmobilepingpongapp.sensor.createMotionSensor

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Create motion sensor and view model
        val motionSensor = remember { createMotionSensor() }
        val viewModel = remember { ControllerViewModel(motionSensor) }

        ControllerScreen(viewModel = viewModel)
    }
}