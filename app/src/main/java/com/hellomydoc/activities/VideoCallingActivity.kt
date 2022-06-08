package com.hellomydoc.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.hellomydoc.R

class VideoCallingActivity : AppCompatActivity() {
    private var iv_back: View? = null
    private var fab_mute: ExtendedFloatingActionButton? = null
    private var fab_end: ExtendedFloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_calling)
        iv_back = findViewById(R.id.iv_back)
        fab_mute = findViewById(R.id.fab_mute)
        fab_end = findViewById(R.id.fab_end)
        iv_back?.setOnClickListener {
            finish()
        }
    }
}