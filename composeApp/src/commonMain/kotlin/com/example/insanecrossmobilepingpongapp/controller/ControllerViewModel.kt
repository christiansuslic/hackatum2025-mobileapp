package com.example.insanecrossmobilepingpongapp.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insanecrossmobilepingpongapp.model.ControllerState
import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import com.example.insanecrossmobilepingpongapp.model.PaddleControl
import com.example.insanecrossmobilepingpongapp.network.WebSocketClient
import com.example.insanecrossmobilepingpongapp.sensor.MotionSensor
import com.example.insanecrossmobilepingpongapp.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.math.sqrt

/**
 * ViewModel for managing controller state and processing motion sensor data.
 */
class ControllerViewModel(
    private val motionSensor: MotionSensor
) : ViewModel() {

    private val _state = MutableStateFlow(ControllerState())
    val state: StateFlow<ControllerState> = _state.asStateFlow()

    private val webSocketClient = WebSocketClient()
    private val swingDetector = SwingDetector()

    // Smoothing parameters
    private val smoothingFactor = 0.8f // Higher = more smoothing
    private val accelSmoothingFactor = 0.7f // Smoothing for acceleration
    private var lastOrientation = DeviceOrientation()

    // Logging control - for periodic motion data logs
    private var processCount = 0
    private val logEveryNProcesses = 50 // Log every 50 processed updates (~1 second at 50Hz)

    init {
        Log.i(TAG, "🎮 ControllerViewModel initializing...")
        Log.i(TAG, "📡 Motion sensor available: ${motionSensor.isAvailable()}")
        Log.i(TAG, "⚙️ Swing detection config: threshold=${SwingDetectionConfig.SWING_SPEED_THRESHOLD}, minInterval=${SwingDetectionConfig.SWING_MIN_INTERVAL_MS}ms")

        // Observe WebSocket connection state
        viewModelScope.launch {
            webSocketClient.connectionState.collect { connectionState ->
                _state.update { it.copy(connectionState = connectionState) }
            }
        }

        if (motionSensor.isAvailable()) {
            startListening()
        } else {
            Log.e(TAG, "❌ Motion sensor not available!")
        }
    }

    /**
     * Start listening to motion sensor updates.
     */
    private fun startListening() {
        Log.i(TAG, "▶️ Starting to listen to motion sensor...")
        viewModelScope.launch {
            motionSensor.orientation.collect { orientation ->
                processOrientation(orientation)
            }
        }
        _state.update { it.copy(isActive = true) }
        Log.i(TAG, "✅ Motion sensor listener active")
    }

    /**
     * Process raw orientation data and update state.
     */
    private fun processOrientation(rawOrientation: DeviceOrientation) {
        // Apply smoothing to reduce jitter
        val smoothedOrientation = smoothOrientation(rawOrientation)

        // Apply calibration offset
        val calibratedOrientation = applyCalibration(smoothedOrientation)

        // Map to paddle control
        val paddleControl = mapToPaddleControl(calibratedOrientation)

        // Periodic logging of motion data (not too spammy)
        processCount++
        if (processCount % logEveryNProcesses == 0) {
            Log.d(TAG, "📊 Motion → pitch: ${toDegrees(calibratedOrientation.pitch.toDouble())}°, roll: ${toDegrees(calibratedOrientation.roll.toDouble())}°, accelMag: ${
            kotlin.math.sqrt(
                calibratedOrientation.accelerationX * calibratedOrientation.accelerationX +
                calibratedOrientation.accelerationY * calibratedOrientation.accelerationY +
                calibratedOrientation.accelerationZ * calibratedOrientation.accelerationZ
            )
        } m/s²")
        Log.d(TAG, "🎯 Control → tiltX: ${paddleControl.tiltX}, tiltY: ${paddleControl.tiltY}, intensity: ${paddleControl.intensity}, swingSpeed: ${paddleControl.swingSpeed}")
    }

    // ALWAYS update UI state (continuous)
    _state.update { currentState ->
        currentState.copy(currentControl = paddleControl)
    }

    // ✨ EVENT-BASED SENDING: Only send over WebSocket when swing is detected
    val currentTime = getCurrentTimeMillis()
    if (swingDetector.detectSwing(paddleControl, currentTime)) {
        // Swing detected! Send minimal swing event with just speed
        webSocketClient.sendSwingEvent(paddleControl.swingSpeed)
    }

    // Store for next smoothing iteration
    lastOrientation = smoothedOrientation
}

/**
 * Get current time in milliseconds (platform-agnostic).
 */
private fun getCurrentTimeMillis(): Long {
    return com.example.insanecrossmobilepingpongapp.util.getCurrentTimeMillis()
}

/**
 * Apply exponential smoothing to orientation and acceleration data.
 */
private fun smoothOrientation(raw: DeviceOrientation): DeviceOrientation {
    return DeviceOrientation(
        pitch = smoothingFactor * lastOrientation.pitch + (1 - smoothingFactor) * raw.pitch,
        roll = smoothingFactor * lastOrientation.roll + (1 - smoothingFactor) * raw.roll,
        yaw = smoothingFactor * lastOrientation.yaw + (1 - smoothingFactor) * raw.yaw,
        accelerationX = accelSmoothingFactor * lastOrientation.accelerationX + (1 - accelSmoothingFactor) * raw.accelerationX,
        accelerationY = accelSmoothingFactor * lastOrientation.accelerationY + (1 - accelSmoothingFactor) * raw.accelerationY,
        accelerationZ = accelSmoothingFactor * lastOrientation.accelerationZ + (1 - accelSmoothingFactor) * raw.accelerationZ
    )
}

/**
 * Apply calibration offset to orientation.
 */
private fun applyCalibration(orientation: DeviceOrientation): DeviceOrientation {
    val offset = _state.value.calibrationOffset
    return DeviceOrientation(
        pitch = orientation.pitch - offset.pitch,
        roll = orientation.roll - offset.roll,
        yaw = orientation.yaw - offset.yaw,
        accelerationX = orientation.accelerationX,
        accelerationY = orientation.accelerationY,
        accelerationZ = orientation.accelerationZ
    )
}

/**
 * Map device orientation and acceleration to normalized paddle control values.
 *
 * Mapping strategy:
 * - Roll (left/right tilt) -> tiltX
 * - Pitch (forward/back tilt) -> tiltY
 * - Intensity based on total tilt magnitude
 * - Acceleration magnitude -> swingSpeed (like swinging a ping pong racket)
 * - Acceleration direction -> swingDirectionX/Y
 */
private fun mapToPaddleControl(orientation: DeviceOrientation): PaddleControl {
    // Maximum tilt angles in radians (about ±45 degrees)
    val maxTiltAngle = kotlin.math.PI.toFloat() / 4

    // Map roll to tiltX (left/right)
    val tiltX = (orientation.roll / maxTiltAngle).coerceIn(-1f, 1f)

    // Map pitch to tiltY (forward/backward)
    val tiltY = (orientation.pitch / maxTiltAngle).coerceIn(-1f, 1f)

    // Calculate intensity based on total tilt magnitude
    val tiltMagnitude = sqrt(tiltX * tiltX + tiltY * tiltY)
    val intensity = tiltMagnitude.coerceIn(0f, 1f)

    // Calculate swing speed from acceleration magnitude
    // Typical phone swing for ping pong: 0-20 m/s²
    val accelMagnitude = sqrt(
        orientation.accelerationX * orientation.accelerationX +
        orientation.accelerationY * orientation.accelerationY +
        orientation.accelerationZ * orientation.accelerationZ
    )
    val maxAcceleration = 5f // m/s² - Lowered from 20f to increase sensitivity
    val swingSpeed = (accelMagnitude / maxAcceleration).coerceIn(0f, 1f)

    // Calculate swing direction (normalized X and Y components)
    val swingDirectionX = if (accelMagnitude > 0.5f) {
        (orientation.accelerationX / accelMagnitude).coerceIn(-1f, 1f)
    } else {
        0f
    }

    val swingDirectionY = if (accelMagnitude > 0.5f) {
        (orientation.accelerationY / accelMagnitude).coerceIn(-1f, 1f)
    } else {
        0f
    }

    return PaddleControl(
        tiltX = tiltX,
        tiltY = tiltY,
        intensity = intensity,
        swingSpeed = swingSpeed,
        swingDirectionX = swingDirectionX,
        swingDirectionY = swingDirectionY
    )
}

/**
 * Calibrate the controller using current orientation as neutral position.
 */
fun calibrate() {
    val currentOrientation = lastOrientation
    Log.i(TAG, "🎚️ Calibrating controller...")
    Log.d(TAG, "📐 Calibration offset → pitch: ${toDegrees(currentOrientation.pitch.toDouble())}°, roll: ${toDegrees(currentOrientation.roll.toDouble())}°, yaw: ${toDegrees(currentOrientation.yaw.toDouble())}°")
    _state.update { currentState ->
            currentState.copy(
                calibrationOffset = currentOrientation,
                isCalibrated = true
            )
        }
        Log.i(TAG, "✅ Controller calibrated")
    }

    /**
     * Reset calibration to default (no offset).
     */
    fun resetCalibration() {
        Log.i(TAG, "🔄 Resetting calibration...")
        _state.update { currentState ->
            currentState.copy(
                calibrationOffset = DeviceOrientation(),
                isCalibrated = false
            )
        }
        Log.i(TAG, "✅ Calibration reset")
    }

    /**
     * Connect to WebSocket server.
     */
    fun connect(url: String, token: String) {
        Log.i(TAG, "🔌 Connecting to server: $url with token: $token")
        _state.update { it.copy(serverUrl = url, token = token) }
        webSocketClient.connect(url, token)
    }

    /**
     * Disconnect from WebSocket server.
     */
    fun disconnect() {
        Log.i(TAG, "🔌 Disconnecting from server")
        webSocketClient.disconnect()
    }

    /**
     * Update server URL.
     */
    fun updateServerUrl(url: String) {
        _state.update { it.copy(serverUrl = url) }
    }

    /**
     * Update authentication token.
     */
    fun updateToken(token: String) {
        _state.update { it.copy(token = token) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "🛑 ViewModel clearing - stopping motion sensor and WebSocket")
        motionSensor.stop()
        webSocketClient.close()
    }

    companion object {
        private const val TAG = "ControllerViewModel"
    }

    private fun toDegrees(radians: Double): Double {
        return radians * 180.0 / kotlin.math.PI
    }
}
