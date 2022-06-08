package com.hellomydoc.activities

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hellomydoc.*
import com.hellomydoc.data.RegistrationData
import com.hellomydoc.databinding.ActivityRegisterBinding
import com.xeinebiu.suspend.dialogs.SuspendAlertDialog
import kotlinx.coroutines.launch

class RegisterActivity : AbstractActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        onCreateActions()
    }

    private fun onCreateActions() {
        setupGoogleLoginClickAction()
        setupFacebookLoginClickAction()
        binding.signUpBtn.setOnClickListener {
            trySignup()
        }
        binding.tvLogin.setOnClickListener {
            goBackToLogin()
        }
    }

    private fun setupFacebookLoginClickAction() {
        binding.fbLoginImgBtn.setOnClickListener {
            FbLogin { success, ex, email, name, imageUrl, id ->
                doFacebookLogin(email, name, imageUrl ?: "", id)
            }.login()
        }
    }

    private fun setupGoogleLoginClickAction() {
        binding.googleLoginImgBtn.setOnClickListener {
            GLogin.login { email, name, imageUrl, id ->
                if (email != null && name != null && id != null) {
                    doGoogleLogin(email, name, imageUrl ?: "", id)
                }
            }
        }
    }

    private fun doGoogleLogin(email: String, name: String, imageUrl: String, id: String) {
        lifecycleScope.launch {
            wait = true
            try {
                val response = repository.googleLogin(email, name, imageUrl, id).resp
                when{
                    response.isError->response.errorMessage.toastLong(this@RegisterActivity)
                    else->{
                        when{
                            response.isSuccess->{
                                val loginResponse = response.body
                                when{
                                    loginResponse!=null->{
                                        if(loginResponse.success){
                                            val uid = loginResponse.user_id
                                            repository.userUid = uid
                                            repository.loginDone = true
                                            if(loginResponse.mobile.isNotEmpty
                                                &&loginResponse.mobile.isMobile
                                                &&loginResponse.mobile_verified=="1"
                                            ){
                                                gotoHome()
                                                return@launch
                                            }
                                            else{
                                                gotoAddMobile(loginResponse.mobile)
                                            }
                                        }
                                        else{
                                            loginResponse.message.toast(this@RegisterActivity)
                                        }
                                    }
                                    else->{
                                        getString(R.string.something_went_wrong).toast(this@RegisterActivity)
                                    }
                                }
                            }
                            else->{
                                getString(R.string.something_went_wrong).toast(this@RegisterActivity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.toast(this@RegisterActivity)
            }
            wait = false
        }
    }

    private fun doFacebookLogin(email: String, name: String, imageUrl: String, id: String) {
        lifecycleScope.launch {
            wait = true
            try {
                val response = repository.facebookLogin(email, name, imageUrl, id).resp
                when{
                    response.isError->response.errorMessage.toastLong(this@RegisterActivity)
                    else->{
                        when{
                            response.isSuccess->{
                                val loginResponse = response.body
                                when{
                                    loginResponse!=null->{
                                        if(loginResponse.success){
                                            val uid = loginResponse.user_id
                                            repository.userUid = uid
                                            repository.loginDone = true
                                            if(loginResponse.mobile.isNotEmpty
                                                &&loginResponse.mobile.isMobile
                                                &&loginResponse.mobile_verified=="1"
                                            ){
                                                gotoHome()
                                                return@launch
                                            }
                                        }
                                        else{
                                            loginResponse.message.toast(this@RegisterActivity)
                                        }
                                    }
                                    else->{
                                        getString(R.string.something_went_wrong).toast(this@RegisterActivity)
                                    }
                                }
                            }
                            else->{
                                getString(R.string.something_went_wrong).toast(this@RegisterActivity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                getString(R.string.something_went_wrong).toast(this@RegisterActivity)
                e.message?.log("facebook_bug")
            }
            wait = false
        }

    }

    private fun trySignup() {
        val name = binding.etNameCore.text.toString()
        if(name.isEmpty){
            getString(R.string.please_put_your_name).toast(this)
            return
        }
        if(name.isNotPerson){
            getString(R.string.please_put_a_valid_name).toast(this)
        }
        /////////////////////////////////////
        val email = binding.etRegEmailCore.text.toString()
        if(email.isEmpty){
            R.string.please_put_email.string.toast(this)
            return
        }
        if(email.isNotEmail){
            R.string.please_put_a_valid_email.string.toast(this)
            return
        }
        ////////////////////////////////////
        val password = binding.etRegPasswordCore.text?.string?:""
        if(password.isEmpty){
            R.string.please_enter_password.string.toast(this)
            return
        }
        if(password.isNotPassword){
            lifecycleScope.launch {
                SuspendAlertDialog.alert(R.string.ok.string) {
                    MaterialAlertDialogBuilder(this@RegisterActivity)
                        .setTitle(R.string.app_name.string)
                        .setMessage(R.string.password_pattern_warning.string)
                }
            }
            return
        }
        ///////////////////////////////
        val confirmPassword = binding.etRegConPasswordCore.text?.string?:""
        if(confirmPassword.isEmpty){
            R.string.please_enter_confirm_password.string.toast(this)
            return
        }
        if(confirmPassword!=password){
            R.string.confirm_password_does_not_match.string.toast(this)
            return
        }
        val registrationData = RegistrationData(
            name = name,
            email = email,
            password = password
        )
        lifecycleScope.launch {
            wait = true
            processApi {
                repository.register(registrationData).resp
            }.apply {
                when(status){
                    ApiDispositionStatus.EXCEPTION -> exception?.message?.toastLong(this@RegisterActivity)
                    ApiDispositionStatus.ERROR -> error?.toast(this@RegisterActivity)

                    ApiDispositionStatus.UNKNOWN,
                    ApiDispositionStatus.BODY_NULL,
                    ApiDispositionStatus.FAILED -> R.string.something_went_wrong.string.toast(this@RegisterActivity)

                    ApiDispositionStatus.RESPONSE -> {
                        response?.apply {
                            when{
                                success==false->{
                                    message.toast(this@RegisterActivity)
                                }
                                success->{
                                    repository.userUid = user_id
                                    gotoAddMobile("")
                                }
                                else->{
                                    message.toast(this@RegisterActivity)
                                }
                            }
                        }
                    }
                }
            }
            wait = false
        }
    }
}