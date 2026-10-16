package org.fedroad.net.sensing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PreprocessorTest {

    private fun window(samples: List<SensorSample>) =
        EventWindow(samples = samples, triggerIndex = samples.size - 1, triggerTimestampNanos = 0L)

    private fun samples(count: Int, x: Float = 0f, y: Float = 0f, z: Float = 9.81f) =
        (0 until count).map { SensorSample(ts(it), x, y, z) }

    private fun ts(i: Int) = i.toLong()

    @Test
    fun outputLengthIsWindowSizeTimesThree() {
        val preprocessor = Preprocessor(windowSizeSamples = 8)
        val result = preprocessor.toModelInput(window(samples(8)))
        assertEquals(24, result.size)
    }

    @Test
    fun rawModeInterleavesAxesWithoutGravityRemoval() {
        val preprocessor = Preprocessor(windowSizeSamples = 2, removeGravity = false)
        val result = preprocessor.toModelInput(
            window(
                listOf(
                    SensorSample(0L, 1f, 2f, 3f),
                    SensorSample(1L, 4f, 5f, 6f),
                ),
            ),
        )
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f, 6f), result.toList())
    }

    @Test
    fun gravityRemovalYieldsNearZeroForConstantSignal() {
        val preprocessor = Preprocessor(windowSizeSamples = 20, removeGravity = true)
        val result = preprocessor.toModelInput(window(samples(20, x = 0f, y = 0f, z = 9.81f)))
        // All components after the first sample should be close to zero.
        for (i in 1 until 20) {
            assertTrue("x[$i] should be ~0", kotlin.math.abs(result[i * 3]) < 0.5f)
            assertTrue("y[$i] should be ~0", kotlin.math.abs(result[i * 3 + 1]) < 0.5f)
            assertTrue("z[$i] should be ~0", kotlin.math.abs(result[i * 3 + 2]) < 0.5f)
        }
    }

    @Test
    fun scaleIsApplied() {
        val preprocessor = Preprocessor(windowSizeSamples = 1, removeGravity = false, scale = 2f)
        val result = preprocessor.toModelInput(window(listOf(SensorSample(0L, 1f, 2f, 3f))))
        assertEquals(listOf(2f, 4f, 6f), result.toList())
    }

    @Test
    fun wrongSampleCountIsRejected() {
        val preprocessor = Preprocessor(windowSizeSamples = 4)
        val bad = window(samples(3))
        assertThrows(IllegalArgumentException::class.java) {
            preprocessor.toModelInput(bad)
        }
    }
}
