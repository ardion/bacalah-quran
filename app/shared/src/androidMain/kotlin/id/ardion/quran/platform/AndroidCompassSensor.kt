package id.ardion.quran.platform

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
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

private const val ACCURACY_HIGH = SensorManager.SENSOR_STATUS_ACCURACY_HIGH

private const val MAX_FLAT_TILT_DEGREES = 45.0

private const val RAW_SAMPLE_FILTER = 0.8f

private const val FINE_TURN_DEGREES = 1.5f
private const val FINE_SMOOTHING = 0.15f
private const val COARSE_SMOOTHING = 0.35f

class AndroidCompassSensor(private val context: Context) : CompassSensor, SensorEventListener {

    private val _azimuth = MutableStateFlow(0f)
    override val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _accuracy = MutableStateFlow(ACCURACY_HIGH)
    override val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    private val _isFlat = MutableStateFlow(true)
    override val isFlat: StateFlow<Boolean> = _isFlat.asStateFlow()

    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var geomagneticField: GeomagneticField? = null

    override val isAvailable: Boolean = sensorManager?.let { manager ->
        val hasRotation = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
            manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) != null
        val hasRawSensors = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
            manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
        hasRotation || hasRawSensors
    } ?: false

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val adjustedMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    override fun setLocation(latitude: Double, longitude: Double) {
        geomagneticField = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            0f,
            System.currentTimeMillis()
        )
    }

    override fun start() {
        val manager = sensorManager ?: return
        val rotationSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        if (rotationSensor != null) {
            manager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD).forEach { type ->
                manager.getDefaultSensor(type)?.let {
                    manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    override fun stop() {
        sensorManager?.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.accuracy >= 0) _accuracy.value = event.accuracy

        val hasRotation = when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                true
            }
            Sensor.TYPE_ACCELEROMETER -> {
                lowPass(event.values, gravity)
                hasGravity = true
                rotationFromRawSensors()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lowPass(event.values, geomagnetic)
                hasGeomagnetic = true
                rotationFromRawSensors()
            }
            else -> false
        }

        if (hasRotation) updateOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (sensor?.type) {
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
            Sensor.TYPE_MAGNETIC_FIELD -> _accuracy.value = accuracy
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray) {
        for (i in output.indices) {
            output[i] = RAW_SAMPLE_FILTER * output[i] + (1 - RAW_SAMPLE_FILTER) * (input.getOrNull(i) ?: 0f)
        }
    }

    private fun rotationFromRawSensors(): Boolean =
        hasGravity && hasGeomagnetic &&
            SensorManager.getRotationMatrix(rotationMatrix, FloatArray(9), gravity, geomagnetic)

    private fun updateOrientation() {
        remapForDisplayRotation(rotationMatrix, adjustedMatrix)
        SensorManager.getOrientation(adjustedMatrix, orientation)

        val pitchDeg = Math.toDegrees(orientation[1].toDouble())
        val rollDeg = Math.toDegrees(orientation[2].toDouble())
        _isFlat.value = abs(pitchDeg) <= MAX_FLAT_TILT_DEGREES && abs(rollDeg) <= MAX_FLAT_TILT_DEGREES

        val magneticAzimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        val declination = geomagneticField?.declination ?: 0f
        val trueAzimuth = (magneticAzimuth + declination + 360f) % 360f

        val current = _azimuth.value
        var diff = trueAzimuth - current
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        val smoothing = if (abs(diff) < FINE_TURN_DEGREES) FINE_SMOOTHING else COARSE_SMOOTHING
        _azimuth.value = (current + diff * smoothing + 360f) % 360f
    }

    private fun remapForDisplayRotation(source: FloatArray, target: FloatArray) {
        val (axisX, axisY) = when (displayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> {
                source.copyInto(target)
                return
            }
        }
        SensorManager.remapCoordinateSystem(source, axisX, axisY, target)
    }

    private fun displayRotation(): Int =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.rotation ?: Surface.ROTATION_0
            } else {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                // defaultDisplay is the only option below API 30.
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        } catch (_: UnsupportedOperationException) {
            Surface.ROTATION_0
        }
}
