package org.fedroad.net.sensing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventWindowExtractorTest {

    private val gravity = 9.81f

    private fun calm(ts: Long, jitter: Float = 0f) =
        SensorSample(ts, jitter, 0f, gravity)

    private fun oscillating(ts: Long, index: Int) =
        SensorSample(ts, if (index % 2 == 0) 5f else -5f, 0f, gravity)

    private fun eventConfig(
        window: Int = 5,
        cooldown: Int = 0,
        threshold: Float = 2.5f,
    ) = WindowConfig(
        sampleRateHz = 50,
        windowSizeSamples = window,
        cooldownSamples = cooldown,
        triggerThresholdMs2 = threshold,
        gravityFilterAlpha = 0.8f,
    )

    @Test
    fun noWindowIsEmittedUntilBufferIsFull() {
        val extractor = EventWindowExtractor(eventConfig(window = 5))
        for (i in 0 until 4) {
            assertNull(extractor.add(calm(ts = i.toLong())))
        }
        assertEquals(4L, extractor.samplesSeen)
        assertEquals(0L, extractor.windowsEmitted)
    }

    @Test
    fun calmSamplesNeverTrigger() {
        val extractor = EventWindowExtractor(eventConfig())
        for (i in 0 until 100) {
            assertNull(extractor.add(calm(ts = i.toLong())))
        }
        assertEquals(0L, extractor.windowsEmitted)
    }

    @Test
    fun spikeTriggersWindowWithCorrectLengthAndTriggerIndex() {
        val extractor = EventWindowExtractor(eventConfig(window = 5))
        for (i in 0 until 4) {
            assertNull(extractor.add(calm(ts = i.toLong())))
        }
        val window = extractor.add(SensorSample(4L, 0f, 0f, gravity + 10f))

        assertNotNull(window)
        val w = window!!
        assertEquals(5, w.samples.size)
        assertEquals(4, w.triggerIndex)
        assertEquals(4L, w.triggerTimestampNanos)
        assertEquals(0f, w.samples.last().x, 0.0001f)
        assertEquals(gravity + 10f, w.samples.last().z, 0.0001f)
    }

    @Test
    fun bufferStaysBoundedToWindowSize() {
        val extractor = EventWindowExtractor(eventConfig(window = 5))
        for (i in 0 until 50) {
            extractor.add(calm(ts = i.toLong(), jitter = (i % 3).toFloat() * 0.1f))
        }
        val window = extractor.add(SensorSample(50L, 0f, 0f, gravity + 20f))
        assertNotNull(window)
        assertEquals(5, window!!.samples.size)
    }

    @Test
    fun cooldownSuppressesDuplicateTriggers() {
        // An oscillating signal keeps linear acceleration above the tiny
        // threshold, so triggers would fire every sample without a cooldown.
        val extractor = EventWindowExtractor(
            eventConfig(window = 3, cooldown = 2, threshold = 0.0001f),
        )

        assertNull(extractor.add(oscillating(1L, 0)))
        assertNull(extractor.add(oscillating(2L, 1)))
        assertNotNull(extractor.add(oscillating(3L, 2)))  // trigger #1 (buffer full)
        assertNull(extractor.add(oscillating(4L, 3)))     // cooldown 2 -> 1
        assertNull(extractor.add(oscillating(5L, 4)))     // cooldown 1 -> 0
        assertNotNull(extractor.add(oscillating(6L, 5)))  // trigger #2

        assertEquals(2L, extractor.windowsEmitted)
    }

    @Test
    fun resetClearsCounters() {
        val extractor = EventWindowExtractor(eventConfig())
        repeat(10) { extractor.add(calm(ts = it.toLong())) }
        extractor.reset()
        assertEquals(0L, extractor.samplesSeen)
        assertEquals(0L, extractor.windowsEmitted)
    }

    @Test
    fun gravityRemovalReducesStaticReading() {
        val extractor = EventWindowExtractor(eventConfig())
        repeat(200) { extractor.add(calm(ts = it.toLong())) }
        val linear = extractor.linearAcceleration(SensorSample(200L, 0f, 0f, gravity))
        assertTrue("linear magnitude should be near zero", kotlin.math.abs(linear[2]) < 0.5f)
    }
}
