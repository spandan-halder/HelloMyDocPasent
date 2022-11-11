package com.hellomydoc.updated_token

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hellomydoc.Prefs.getString
import com.hellomydoc.R

class MyFirebaseToken {


    fun myUpdateToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                //Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            updatedToken = task.result

            // Log and toast

            Log.d("HelloToken", task.result)
            //
        })
    }

    init {
        myUpdateToken()
    }

    companion object{
      final var updatedToken:String=""
    }
}