package org.fedroad.net.ml

/**
 * Contract for on-device inference. The implementation is provided in Phase 5
 * when a concrete TFLite model exists.
 */
interface InferenceEngine {
    /** Returns true if the model is loaded and ready for inference. */
    val isReady: Boolean

    /**
     * Run inference on the flattened input array. The [input] layout and shape
     * must match the model contract (e.g. axis-interleaved window of 3 axes).
     */
    fun infer(input: FloatArray): InferenceResult

    /** Release any native resources. */
    fun close()
}
