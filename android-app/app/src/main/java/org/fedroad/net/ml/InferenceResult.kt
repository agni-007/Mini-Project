package org.fedroad.net.ml

/**
 * TensorFlow Lite inference result for road-hazard classification.
 *
 * Values are floating-point probabilities in [0, 1]. Labels are defined in the
 * model contract once a trained model exists. This is intentionally generic so
 * we can adapt to different label sets later.
 */
data class InferenceResult(
    val probabilities: FloatArray,
    val inferenceTimeMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InferenceResult

        if (!probabilities.contentEquals(other.probabilities)) return false
        if (inferenceTimeMs != other.inferenceTimeMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = probabilities.contentHashCode()
        result = 31 * result + inferenceTimeMs.hashCode()
        return result
    }
}
