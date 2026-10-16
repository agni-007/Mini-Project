package org.fedroad.net.ml

import org.fedroad.net.sensing.EventWindow
import org.fedroad.net.sensing.Preprocessor

/**
 * A no-op inference engine used as a placeholder until Phase 5 provides a real
 * TFLite-backed implementation.
 *
 * It never performs computation and is safe to use in tests and in the debug
 * UI while the model/artifact is not yet present. When a model is added, this
 * class can be replaced by `TFLiteInferenceEngine`.
 *
 * Note: do NOT fabricate model outputs. If the engine is not ready or no model
 * exists, callers must detect [isReady] == false.
 */
class PlaceholderInferenceEngine : InferenceEngine {
    override val isReady: Boolean = false

    override fun infer(input: FloatArray): InferenceResult {
        // Intentionally returns empty/zero-like result to avoid fabricating
        // predictions. Callers should check isReady before calling infer.
        return InferenceResult(
            probabilities = FloatArray(0),
            inferenceTimeMs = 0L,
        )
    }

    override fun close() {
        // No resources to release.
    }

    companion object {
        /** Convenience helper to run preprocessing + inference in one call. */
        fun run(preprocessor: Preprocessor, window: EventWindow): InferenceResult {
            val engine = PlaceholderInferenceEngine()
            if (!engine.isReady) {
                return InferenceResult(FloatArray(0), 0L)
            }
            return engine.infer(preprocessor.toModelInput(window))
        }
    }
}
