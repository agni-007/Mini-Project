package org.fedroad.net.sensing

/**
 * Converts an [EventWindow] into a flat `FloatArray` for on-device inference.
 *
 * Output contract (see `docs/MODEL_CONTRACT.md`):
 *  - layout: axis-interleaved, `[x0, y0, z0, x1, y1, z1, ...]`, oldest sample first
 *  - length: `windowSizeSamples * 3`
 *  - values: gravity-removed linear acceleration in m/s^2 (if enabled), times [scale]
 *
 * No model-specific normalisation constants are invented here. If the trained
 * model in Phase 4 requires mean/std normalisation or a particular scale, those
 * constants must be fixed in the model contract and passed via constructor
 * arguments here — not hard-coded.
 */
class Preprocessor(
    private val windowSizeSamples: Int,
    private val removeGravity: Boolean = true,
    private val gravityFilterAlpha: Float = 0.8f,
    val scale: Float = 1.0f,
) {
    init {
        require(windowSizeSamples > 0) { "windowSizeSamples must be > 0" }
        require(gravityFilterAlpha in 0f..1f) { "gravityFilterAlpha must be in [0,1]" }
        require(scale > 0f) { "scale must be > 0" }
    }

    /** Number of floats produced per window. */
    val inputLength: Int
        get() = windowSizeSamples * 3

    fun toModelInput(window: EventWindow): FloatArray {
        val samples = window.samples
        require(samples.size == windowSizeSamples) {
            "expected $windowSizeSamples samples, got ${samples.size}"
        }

        val out = FloatArray(windowSizeSamples * 3)
        var gx = samples[0].x
        var gy = samples[0].y
        var gz = samples[0].z
        val a = gravityFilterAlpha

        for (i in samples.indices) {
            val s = samples[i]
            gx = a * gx + (1f - a) * s.x
            gy = a * gy + (1f - a) * s.y
            gz = a * gz + (1f - a) * s.z

            val x = if (removeGravity) s.x - gx else s.x
            val y = if (removeGravity) s.y - gy else s.y
            val z = if (removeGravity) s.z - gz else s.z

            out[i * 3] = x * scale
            out[i * 3 + 1] = y * scale
            out[i * 3 + 2] = z * scale
        }
        return out
    }
}
