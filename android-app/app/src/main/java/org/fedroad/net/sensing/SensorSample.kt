package org.fedroad.net.sensing

import kotlin.math.sqrt

/**
 * A single raw accelerometer sample.
 *
 * [x], [y], [z] are in m/s² as reported by `android.hardware.Sensor.TYPE_ACCELEROMETER`,
 * which includes gravity. [timestampNanos] is the sensor event timestamp in nanoseconds.
 *
 * Privacy: raw samples are processed and kept on-device only.
 */
data class SensorSample(
    val timestampNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
) {
    val magnitude: Float
        get() = kotlin.math.sqrt(x * x + y * y + z * z)
}
