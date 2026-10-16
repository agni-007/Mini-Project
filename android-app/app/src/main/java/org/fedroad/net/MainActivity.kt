package org.fedroad.net

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

/**
 * Phase 2 skeleton activity.
 *
 * Sensor capture, on-device inference, and the consent UI are added in later
 * phases. This only proves the project builds and runs.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.status_text).text =
            getString(R.string.phase2_status)
    }
}
