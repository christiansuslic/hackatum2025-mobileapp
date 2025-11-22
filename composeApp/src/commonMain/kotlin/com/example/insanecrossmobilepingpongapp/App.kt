package com.example.insanecrossmobilepingpongapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.insanecrossmobilepingpongapp.controller.ControllerViewModel
import com.example.insanecrossmobilepingpongapp.model.Screen
import com.example.insanecrossmobilepingpongapp.sensor.MotionSensor
import com.example.insanecrossmobilepingpongapp.sensor.createMotionSensor
import com.example.insanecrossmobilepingpongapp.ui.DebugOverlay
import com.example.insanecrossmobilepingpongapp.ui.GameScreen
import com.example.insanecrossmobilepingpongapp.ui.MenuScreen

@Composable
@Preview
fun App(
    motionSensor: MotionSensor = remember { createMotionSensor() }
) {
    MaterialTheme {
        // Create view model with injected sensor
        val viewModel = remember { ControllerViewModel(motionSensor) }

        // Observe UI state
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Navigation based on current screen
        when (state.currentScreen) {
            Screen.Menu -> {
                MenuScreen(
                    onPlayerSelected = { playerRole ->
                        viewModel.selectPlayer(playerRole)
                    }
                )
            }

            Screen.Game -> {
                // Ensure player role is set before showing game screen
                state.playerRole?.let { playerRole ->
                    GameScreen(
                        playerRole = playerRole,
                        connectionState = state.connectionState,
                        isDebugVisible = state.isDebugVisible,
                        onToggleDebug = { viewModel.toggleDebug() },
                        onDisconnect = { viewModel.disconnectAndReturnToMenu() },
                        debugContent = {
                            DebugOverlay(
                                connectionState = state.connectionState,
                                serverUrl = state.serverUrl,
                                isCalibrated = state.isCalibrated,
                                isActive = state.isActive,
                                paddleControl = state.currentControl,
                                onCalibrate = { viewModel.calibrate() },
                                onClose = { viewModel.toggleDebug() }
                            )
                        }
                    )
                }
            }
        }
    }
}