package com.hellomydoc

import android.os.Bundle
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import java.util.*

class FbLogin(private var callback: ((Boolean,Exception?,String,String,String,String)->Unit)?) {
    private var callbackManager: CallbackManager = CallbackManager.Factory.create()
    init {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request = GraphRequest.newMeRequest(
                        loginResult!!.accessToken
                    ) { data, response ->
                        var email = data?.getString("email")
                        var name = data?.getString("name")
                        var id = data?.getString("id")
                        var imageUrl = "https://graph.facebook.com/$id/picture"

                        callback?.let {
                            if(App.currentActivity !==null)
                            {
                                it(true,null,email!!,name!!,imageUrl,id!!)
                            }
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    callback?.let {
                        if(App.currentActivity !==null)
                        {
                            it(false,null,"","","","")
                        }
                    }
                }

                override fun onError(exception: FacebookException) {
                    callback?.let {
                        if(App.currentActivity !==null)
                        {
                            it(false,exception,"","","","")
                        }
                    }
                }


            })


        App.currentActivity?.addActivityResultCallback { requestCode, resultCode, data ->
            callbackManager.onActivityResult(requestCode,resultCode,data)
        }
    }

    fun login()
    {



        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                    LoginManager.getInstance().logInWithReadPermissions(App.currentActivity!!, Arrays.asList("email"))
                }
            ).executeAsync()
        }
        else{
            LoginManager.getInstance().logInWithReadPermissions(App.currentActivity!!, Arrays.asList("email"))
        }



    }
}