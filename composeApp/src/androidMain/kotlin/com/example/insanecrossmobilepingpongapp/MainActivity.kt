package com.example.insanecrossmobilepingpongapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.insanecrossmobilepingpongapp.controller.ControllerViewModel
import com.example.insanecrossmobilepingpongapp.sensor.AndroidMotionSensor
import com.example.insanecrossmobilepingpongapp.ui.ControllerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AndroidApp()
        }
    }
}

@Composable
fun AndroidApp() {
    MaterialTheme {
        val context = LocalContext.current
        val motionSensor = remember { AndroidMotionSensor(context) }
        val viewModel = remember { ControllerViewModel(motionSensor) }

        ControllerScreen(viewModel = viewModel)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AndroidApp()
}