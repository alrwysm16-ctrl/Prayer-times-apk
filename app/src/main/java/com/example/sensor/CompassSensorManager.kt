package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class CompassSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _accuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(false)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var rotationVectorSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var currentFilteredAzimuth = 0f
    private var lastVibratedAligned = false

    fun startListening() {
        val sm = sensorManager ?: return

        rotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) {
            sm.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            _isSensorAvailable.value = true
        } else {
            accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accelerometer != null && magnetometer != null) {
                sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
                _isSensorAvailable.value = true
            } else {
                _isSensorAvailable.value = false
            }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
            updateSmoothAzimuth(azimuthDeg)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            computeOrientationFromAccMag()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            computeOrientationFromAccMag()
        }
    }

    private fun computeOrientationFromAccMag() {
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
            updateSmoothAzimuth(azimuthDeg)
        }
    }

    private fun updateSmoothAzimuth(targetDeg: Float) {
        // Handle angle wrap-around for 360 to 0 degrees
        var diff = targetDeg - currentFilteredAzimuth
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f

        // Exponential smoothing factor
        val alpha = 0.18f
        currentFilteredAzimuth = (currentFilteredAzimuth + alpha * diff + 360f) % 360f
        _azimuth.value = currentFilteredAzimuth
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracy.value = accuracy
    }

    fun checkAndTriggerHaptic(isAligned: Boolean) {
        if (isAligned && !lastVibratedAligned) {
            triggerVibration()
        }
        lastVibratedAligned = isAligned
    }

    private fun triggerVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45)
                }
            }
        } catch (_: Exception) {
        }
    }
}
