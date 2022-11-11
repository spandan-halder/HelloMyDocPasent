package com.hellomydoc.activities

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hellomydoc.Constants
import com.hellomydoc.R
import com.hellomydoc.data.AppointmentData
import com.vxplore.audiovideocall.videocall.VideoBox
import com.vxplore.audiovideocall.videocall.models.AllowedResponse
import com.vxplore.audiovideocall.videocall.models.Ids

class TestVideoCallActivity : AppCompatActivity() {
    private lateinit var bt_video: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_video_call)
        val data = intent.extras?.getBundle(Constants.DataPayload) as Bundle
        data.apply {
            openVideo(get("data") as AppointmentData)
        }
    }

    private fun openVideo(data: AppointmentData) {
        VideoBox.callback = object : VideoBox.Callback {
            override val ids: Ids?
                get() = Ids(
                    data.patientId!!,
                    data.doctorId!!,
                    data.id!!,
                    data.patientId,
                    data.doctorName!!
                )
            override val appContext: Application
                get() = this@TestVideoCallActivity.application

            override fun onApproving() {

            }

            override suspend fun checkAllowed(channelId: String, userId: String): AllowedResponse? {
                val startTime = System.currentTimeMillis()
                return AllowedResponse(true, "allowed", startTime, 15 * 60 * 1000)
            }
        }
        VideoBox.start(this, data.id ?: return, data.patientId ?: return,
            Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun onBackPressed() {
        finish()
    }
}