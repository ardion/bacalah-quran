package id.ardion.quran.utils

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

actual class CompassSensor : SensorEventListener {
    private val _azimuth = MutableStateFlow(0f)
    actual val azimuth: StateFlow<Float> = _azimuth

    private val _accuracy = MutableStateFlow(3)
    actual val accuracy: StateFlow<Int> = _accuracy

    private val _isFlat = MutableStateFlow(true)
    actual val isFlat: StateFlow<Boolean> = _isFlat

    private var sensorManager: SensorManager? = null
    private var geomagneticField: GeomagneticField? = null

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val adjustedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    actual fun setLocation(latitude: Double, longitude: Double, altitude: Double) {
        try {
            geomagneticField = GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                altitude.toFloat(),
                System.currentTimeMillis()
            )
        } catch (_: Throwable) {}
    }

    actual fun start() {
        val context = ContextHolder.appContext ?: return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    actual fun stop() {
        sensorManager?.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.accuracy >= 0) {
            _accuracy.value = event.accuracy
        }

        var calculated = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
            event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
        ) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            calculated = true
        } else {
            val alpha = 0.8f
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
                hasGravity = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
                hasGeomagnetic = true
            }

            if (hasGravity && hasGeomagnetic) {
                val inclinationMatrix = FloatArray(9)
                if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
                    calculated = true
                }
            }
        }

        if (calculated) {
            processOrientationMatrix(rotationMatrix)
        }
    }

    private fun processOrientationMatrix(sourceMatrix: FloatArray) {
        val context = ContextHolder.appContext
        val displayRotation = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context?.display?.rotation ?: Surface.ROTATION_0
            } else {
                val wm = context?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        } catch (_: Throwable) {
            Surface.ROTATION_0
        }

        when (displayRotation) {
            Surface.ROTATION_90 -> {
                SensorManager.remapCoordinateSystem(
                    sourceMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    adjustedMatrix
                )
            }
            Surface.ROTATION_180 -> {
                SensorManager.remapCoordinateSystem(
                    sourceMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    adjustedMatrix
                )
            }
            Surface.ROTATION_270 -> {
                SensorManager.remapCoordinateSystem(
                    sourceMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    adjustedMatrix
                )
            }
            else -> {
                System.arraycopy(sourceMatrix, 0, adjustedMatrix, 0, 9)
            }
        }

        SensorManager.getOrientation(adjustedMatrix, orientation)
        val azimuthRad = orientation[0]
        val pitchRad = orientation[1]
        val rollRad = orientation[2]

        val pitchDeg = Math.toDegrees(pitchRad.toDouble())
        val rollDeg = Math.toDegrees(rollRad.toDouble())
        _isFlat.value = abs(pitchDeg) <= 45.0 && abs(rollDeg) <= 45.0

        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
        azimuthDeg = (azimuthDeg + 360f) % 360f

        val declination = geomagneticField?.declination ?: 0f
        val trueNorthAzimuth = (azimuthDeg + declination + 360f) % 360f

        val current = _azimuth.value
        var diff = trueNorthAzimuth - current
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f

        val smoothingFactor = if (abs(diff) < 1.5f) 0.15f else 0.35f
        _azimuth.value = (current + diff * smoothingFactor + 360f) % 360f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR ||
            sensor?.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR ||
            sensor?.type == Sensor.TYPE_MAGNETIC_FIELD
        ) {
            _accuracy.value = accuracy
        }
    }
}
