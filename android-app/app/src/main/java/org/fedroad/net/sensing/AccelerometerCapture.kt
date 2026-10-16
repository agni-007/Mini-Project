package org.fedroad.net.sensing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Android accelerometer capture that feeds an [EventWindowExtractor].
 *
 * Responsibilities:
 *  - register/unregister the accelerometer listener (lifecycle-safe)
 *  - convert [SensorEvent]s into [SensorSample]s
 *  - hand triggered [EventWindow]s to [onWindow]
 *
 * Raw samples and windows are kept in memory on-device only; this class never
 * performs any network I/O.
 *
 * Not unit-tested on the JVM (it depends on the Android framework). The pure
 * windowing/preprocessing logic it delegates to is unit-tested separately.
 *
 * @param onWindow called on the sensor thread for each triggered window.
 */
class AccelerometerCapture(
    context: Context,
    private val config: WindowConfig = WindowConfig(),
    private val onWindow: (EventWindow) -> Unit,
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val extractor = EventWindowExtractor(config)

    /** Whether the device exposes an accelerometer. */
    val isAvailable: Boolean
        get() = accelerometer != null

    val samplesSeen: Long
        get() = extractor.samplesSeen

    val windowsEmitted: Long
        get() = extractor.windowsEmitted

    /** Register the listener. Returns false if no accelerometer is available. */
    fun start(): Boolean {
        val sensor = accelerometer ?: return false
        val periodUs = (1_000_000L / config.sampleRateHz).toInt()
        return sensorManager.registerListener(this, sensor, periodUs, periodUs)
    }

    /** Unregister the listener. Safe to call when not started. */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val sample = SensorSample(
            timestampNanos = event.timestamp,
            x = event.values[0],
            y = event.values[1],
            z = event.values[2],
        )
        extractor.add(sample)?.let(onWindow)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op: accuracy changes do not affect window extraction.
    }
}
