package com.hellomydoc
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hellomydoc.chat.ChatBox

class NotificationService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        ChatBox.onNewToken(this.application as App,p0)
    }


    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("new_message",p0.toString())
    }
}