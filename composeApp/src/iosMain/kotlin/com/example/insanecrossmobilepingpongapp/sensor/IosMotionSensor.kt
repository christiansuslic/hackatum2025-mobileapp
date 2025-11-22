package com.example.insanecrossmobilepingpongapp.sensor

import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import com.example.insanecrossmobilepingpongapp.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.sqrt
import kotlin.math.PI

@OptIn(ExperimentalForeignApi::class)
class IosMotionSensor : MotionSensor {
    private val motionManager = CMMotionManager()
    private var updateCount = 0
    private val logEveryNUpdates = 50 // Log every 50 updates (~1 second at 50Hz)

    override val orientation: Flow<DeviceOrientation> = callbackFlow {
        Log.i(TAG, "🎮 Motion sensor starting...")
        Log.i(TAG, "📱 Device motion available: ${motionManager.deviceMotionAvailable}")

        if (!motionManager.deviceMotionAvailable) {
            Log.e(TAG, "❌ Device motion not available!")
            close(IllegalStateException("Device motion not available"))
            return@callbackFlow
        }

        motionManager.deviceMotionUpdateInterval = 0.02 // 50Hz, similar to Android SENSOR_DELAY_GAME
        Log.i(TAG, "⏱️ Update interval set to 50Hz (0.02s)")

        motionManager.startDeviceMotionUpdatesToQueue(
            NSOperationQueue.mainQueue
        ) { motion, error ->
            if (error != null) {
                Log.e(TAG, "❌ Motion sensor error: ${error.localizedDescription}")
                close(Exception("Motion sensor error: ${error.localizedDescription}"))
                return@startDeviceMotionUpdatesToQueue
            }

            motion?.let {
                // CMDeviceMotion provides attitude with pitch, roll, and yaw
                val attitude = it.attitude

                // CMDeviceMotion also provides userAcceleration (excludes gravity)
                val accel = it.userAcceleration

                accel.useContents {
                    val deviceOrientation = DeviceOrientation(
                        pitch = attitude.pitch.toFloat(),
                        roll = attitude.roll.toFloat(),
                        yaw = attitude.yaw.toFloat(),
                        accelerationX = x.toFloat(),
                        accelerationY = y.toFloat(),
                        accelerationZ = z.toFloat()
                    )

                    // Periodic detailed logging
                    updateCount++
                    if (updateCount % logEveryNUpdates == 0) {
                        val accelMagnitude = sqrt(
                            x * x +
                            y * y +
                            z * z
                        ).toFloat()

                        Log.d(TAG, "📊 Orientation → pitch: ${toDegrees(attitude.pitch)}, roll: ${toDegrees(attitude.roll)}, yaw: ${toDegrees(attitude.yaw)}")
                        Log.d(TAG, "🚀 Acceleration → X: $x, Y: $y, Z: $z, |mag|: $accelMagnitude m/s²")
                    }

                    trySend(deviceOrientation)
                }
            }
        }

        Log.i(TAG, "✅ Device motion updates started")

        awaitClose {
            Log.i(TAG, "🛑 Motion sensor stopping...")
            motionManager.stopDeviceMotionUpdates()
            Log.i(TAG, "✅ Device motion updates stopped")
        }
    }

    override fun start() {
        Log.i(TAG, "start() called - Flow handles registration automatically")
    }

    override fun stop() {
        Log.i(TAG, "stop() called - Flow handles unregistration automatically")
    }

    override fun isAvailable(): Boolean {
        val available = motionManager.deviceMotionAvailable
        Log.i(TAG, "isAvailable() = $available")
        return available
    }

    companion object {
        private const val TAG = "IosMotionSensor"
    }

    private fun toDegrees(radians: Double): Double {
        return radians * 180.0 / PI
    }
}

actual fun createMotionSensor(): MotionSensor {
    return IosMotionSensor()
}
