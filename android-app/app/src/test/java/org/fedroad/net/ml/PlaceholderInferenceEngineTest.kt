package org.fedroad.net.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlaceholderInferenceEngineTest {
    @Test
    fun placeholderIsNotReady() {
        val engine = PlaceholderInferenceEngine()
        assertFalse(engine.isReady)
    }

    @Test
    fun inferReturnsEmptyProbabilitiesAndZeroTimeWhenNotReady() {
        val engine = PlaceholderInferenceEngine()
        val result = engine.infer(FloatArray(6))
        assertEquals(0, result.probabilities.size)
        assertEquals(0L, result.inferenceTimeMs)
    }
}
