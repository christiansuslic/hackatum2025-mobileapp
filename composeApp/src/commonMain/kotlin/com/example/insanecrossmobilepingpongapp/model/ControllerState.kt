package com.example.insanecrossmobilepingpongapp.model

import com.example.insanecrossmobilepingpongapp.network.ConnectionState

/**
 * The current state of the motion controller.
 *
 * @property currentControl Current paddle control values
 * @property isCalibrated Whether the controller has been calibrated
 * @property calibrationOffset Calibration offset applied to sensor readings
 * @property isActive Whether motion sensing is currently active
 * @property connectionState WebSocket connection state
 * @property serverUrl WebSocket server URL
 * @property token Authentication token
 */
data class ControllerState(
    val currentControl: PaddleControl = PaddleControl(),
    val isCalibrated: Boolean = false,
    val calibrationOffset: DeviceOrientation = DeviceOrientation(),
    val isActive: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val serverUrl: String = "ws://131.159.222.93:3000",
    val token: String = "player1"  // Default to player1
)
