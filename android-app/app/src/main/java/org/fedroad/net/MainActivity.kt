package org.fedroad.net

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.fedroad.net.sensing.AccelerometerCapture
import org.fedroad.net.sensing.EventWindow

/**
 * Phase 3 skeleton activity.
 *
 * Starts accelerometer capture on resume and stops it on pause, showing capture
 * counts in a debug label. Raw samples stay on-device; nothing is uploaded.
 * On-device inference (Phase 5) will consume the same [EventWindow]s.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private var capture: AccelerometerCapture? = null
    private var eventCount = 0

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
        )
    }
}
