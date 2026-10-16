package org.fedroad.net

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.fedroad.net.ml.PlaceholderInferenceEngine
import org.fedroad.net.sensing.AccelerometerCapture
import org.fedroad.net.sensing.EventWindow
import org.fedroad.net.sensing.Preprocessor

/**
 * Phase 5 scaffold: shows capture stats and whether an on-device inference
 * engine/model is ready. The actual TFLite model and engine will be added when
 * a concrete model is available (Phase 5 complete). No predictions are
 * fabricated when the model is not ready.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private var capture: AccelerometerCapture? = null
    private var eventCount = 0
    private val preprocessor = Preprocessor(windowSizeSamples = 128)
    private val inferenceEngine = PlaceholderInferenceEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)

        val newCapture = AccelerometerCapture(applicationContext) { window: EventWindow ->
            eventCount++
            runOnUiThread {
                this@MainActivity.capture?.let { c ->
                    renderStatus(c, window.samples.size)
                }
            }
        }
        this.capture = newCapture

        if (!newCapture.isAvailable) {
            statusText.text = getString(R.string.sensor_unavailable)
        } else {
            renderStatus(newCapture, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        val capture = capture ?: return
        if (capture.isAvailable && !capture.start()) {
            statusText.text = getString(R.string.sensor_unavailable)
        }
    }

    override fun onPause() {
        capture?.stop()
        super.onPause()
    }

    private fun renderStatus(capture: AccelerometerCapture, lastWindowSize: Int) {
        statusText.text = getString(
            R.string.capture_status,
            capture.samplesSeen,
            eventCount,
            lastWindowSize,
            if (inferenceEngine.isReady) "ready" else "not_ready",
        )
    }
}
