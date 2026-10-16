package org.fedroad.net.sensing

/**
 * Configuration for accelerometer capture and event windowing.
 *
 * All values are explicit and adjustable so the behaviour is explainable and
 * testable. Defaults are a starting point, not a tuned answer.
 *
 * @param sampleRateHz               requested sampling rate in Hz
 * @param windowSizeSamples          number of samples per emitted window
 * @param cooldownSamples            samples suppressed after a trigger to avoid
 *                                   duplicate windows for one physical event
 * @param triggerThresholdMs2        magnitude of gravity-removed linear
 *                                   acceleration (m/s^2) that counts as a trigger
 * @param gravityFilterAlpha         low-pass factor used to estimate gravity
 *                                   (0 = no smoothing, 1 = frozen)
 */
data class WindowConfig(
    val sampleRateHz: Int = 50,
    val windowSizeSamples: Int = 128,
    val cooldownSamples: Int = 64,
    val triggerThresholdMs2: Float = 2.5f,
    val gravityFilterAlpha: Float = 0.8f,
) {
    init {
        require(sampleRateHz > 0) { "sampleRateHz must be > 0" }
        require(windowSizeSamples > 0) { "windowSizeSamples must be > 0" }
        require(cooldownSamples >= 0) { "cooldownSamples must be >= 0" }
        require(triggerThresholdMs2 > 0f) { "triggerThresholdMs2 must be > 0" }
        require(gravityFilterAlpha in 0f..1f) { "gravityFilterAlpha must be in [0,1]" }
    }

    /** Duration of one window in seconds. */
    val windowDurationSeconds: Float
        get() = windowSizeSamples.toFloat() / sampleRateHz
}
