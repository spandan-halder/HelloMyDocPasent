package com.hellomydoc.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.core.os.toPersistableBundle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hellomydoc.Constants
import com.hellomydoc.R
import com.hellomydoc.activities.HomeActivity
import com.hellomydoc.data.AppointmentData

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("MyPushNoti", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("MyPushNoti1", "Message data payload: ${remoteMessage.data}")

            Log.d("MyPushNoti", "Message data payload1")
            sendNotification(remoteMessage.data)
        }

    }

    private fun sendNotification(payload: MutableMap<String, String>) {

        val title = payload["title"]
        val messageBody = payload["body"]
         val argument = AppointmentData(
             doctorId = payload["doctorId"],
             patientId = payload["patientId"],
             category = payload["category"],
             doctorName = payload["doctorName"],
             id = payload["id"],
             image = payload["image"],
             patientName = payload["patientName"],
             prescriptionId = payload["prescriptionId"],
             timestamp = payload["timestamp"],
             type = payload["type"]
         )


        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val dataBundle = bundleOf(
            "title" to title,
            "body" to messageBody,
            "data" to argument
        )
        intent.putExtra(Constants.DataPayload, dataBundle)

        // intent.action = "video_details"//just to make it unique from the next one

        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "My channel ID"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    /*private fun sendNotification(title: String?,messageBody: String?) {

    }*/
}