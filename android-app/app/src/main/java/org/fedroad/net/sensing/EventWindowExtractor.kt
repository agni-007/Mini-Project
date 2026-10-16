package org.fedroad.net.sensing

import kotlin.math.sqrt

/**
 * A fixed-length time window captured around a candidate event.
 *
 * @param samples              exactly `windowSizeSamples` samples, oldest first
 * @param triggerIndex         index of the triggering sample within [samples]
 *                             (currently the last sample)
 * @param triggerTimestampNanos timestamp of the triggering sample
 */
data class EventWindow(
    val samples: List<SensorSample>,
    val triggerIndex: Int,
    val triggerTimestampNanos: Long,
)

/**
 * Event-triggered, fixed-length window extractor.
 *
 * It keeps a bounded buffer of the most recent [WindowConfig.windowSizeSamples]
 * samples. Gravity is tracked with a simple, explainable low-pass filter; a
 * trigger fires when the magnitude of the gravity-removed (linear) acceleration
 * exceeds [WindowConfig.triggerThresholdMs2]. A cooldown suppresses duplicate
 * windows for one physical event.
 *
 * This class has no Android dependencies so it can be unit-tested on the JVM.
 * Raw samples are held in memory only and are never transmitted.
 */
class EventWindowExtractor(private val config: WindowConfig) {

    private val buffer = ArrayDeque<SensorSample>(config.windowSizeSamples)
    private val gravity = floatArrayOf(0f, 0f, 0f)
    private var gravityInitialized = false
    private var cooldownRemaining = 0

    /** Total number of samples ever added (useful for status/debug UI). */
    var samplesSeen: Long = 0L
        private set

    /** Number of windows emitted so far. */
    var windowsEmitted: Long = 0L
        private set

    /**
     * Add one sample. Returns an [EventWindow] if a new window is triggered,
     * otherwise `null`.
     */
    fun add(sample: SensorSample): EventWindow? {
        samplesSeen++
        updateGravity(sample)

        buffer.addLast(sample)
        while (buffer.size > config.windowSizeSamples) {
            buffer.removeFirst()
        }

        if (cooldownRemaining > 0) {
            cooldownRemaining--
            return null
        }
        if (buffer.size < config.windowSizeSamples) {
            return null
        }

        if (linearMagnitude(sample) > config.triggerThresholdMs2) {
            cooldownRemaining = config.cooldownSamples
            windowsEmitted++
            return EventWindow(
                samples = buffer.toList(),
                triggerIndex = buffer.size - 1,
                triggerTimestampNanos = sample.timestampNanos,
            )
        }
        return null
    }

    /** Gravity-removed linear acceleration for [sample]. */
    fun linearAcceleration(sample: SensorSample): FloatArray =
        floatArrayOf(
            sample.x - gravity[0],
            sample.y - gravity[1],
            sample.z - gravity[2],
        )

    /** Clear all state (e.g. on pause or user reset). */
    fun reset() {
        buffer.clear()
        gravity.fill(0f)
        gravityInitialized = false
        cooldownRemaining = 0
        samplesSeen = 0L
        windowsEmitted = 0L
    }

    private fun updateGravity(sample: SensorSample) {
        if (!gravityInitialized) {
            gravity[0] = sample.x
            gravity[1] = sample.y
            gravity[2] = sample.z
            gravityInitialized = true
            return
        }
        val a = config.gravityFilterAlpha
        gravity[0] = a * gravity[0] + (1f - a) * sample.x
        gravity[1] = a * gravity[1] + (1f - a) * sample.y
        gravity[2] = a * gravity[2] + (1f - a) * sample.z
    }

    private fun linearMagnitude(sample: SensorSample): Float {
        val linear = linearAcceleration(sample)
        return sqrt(linear[0] * linear[0] + linear[1] * linear[1] + linear[2] * linear[2])
    }
}
