package com.hellomydoc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class GLogin {
    private var callback: ((String?,String?,String?,String?)->Unit)? = null
    var gso: GoogleSignInOptions? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    private var googleSignInActivityResultRegistration: ActivityResultLauncher<Intent>? = null

    init {
        if(App.currentActivity !=null)
        {
            googleSignInActivityResultRegistration = App.currentActivity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                onGoogleSignInActivityResult(result)
            }
            if(gso==null)
            {
                gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()

                if(App.currentActivity !=null)
                {
                    mGoogleSignInClient = GoogleSignIn.getClient(App.currentActivity as Activity, gso)
                }
            }
        }

    }

    private fun onGoogleSignInActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            var data = result.data
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if(account!=null)
            {
                var email = account.email
                var name = account.displayName
                var imageUrl = account.photoUrl
                var id = account.id
                onSignedIn(email,name,imageUrl,id)

            }
        } catch (e: ApiException) {
        }
    }

    private fun onSignedIn(email: String?, name: String?, imageUrl: Uri?, id: String?) {
        if(callback!=null)
        {
            //App.glogin = null
            callback!!(email,name,imageUrl.toString(),id)
            callback = null
        }
    }

    fun _login(callback: ((String?,String?,String?,String?)->Unit)? = null){
        this.callback = callback
        mGoogleSignInClient?.signOut()
        val signInIntent = mGoogleSignInClient!!.signInIntent
        googleSignInActivityResultRegistration?.launch(signInIntent)
    }

    companion object{
        fun login(callback: ((String?,String?,String?,String?)->Unit)? = null){
            App.glogin?._login(callback)
        }
    }
}