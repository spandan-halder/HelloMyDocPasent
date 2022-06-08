package com.hellomydoc.activities

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hellomydoc.*
import com.hellomydoc.databinding.ActivityLoginBinding
import com.hellomydoc.databinding.ForgotPasswordLayoutBinding
import com.hellomydoc.dialogs.RoundBottomSheet
import kotlinx.coroutines.launch


class LoginActivity : AbstractActivity() {
    var forgotPasswordDialogShown = false
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        onCreatedTasks()
    }

    private fun onCreatedTasks() {
        setupGoogleLoginClickAction()
        setupFacebookLoginClickAction()
        setupLoginClickAction()
        setupRegisterClickAction()
        setupForgotPasswordClickAction()
    }

    private fun setupForgotPasswordClickAction() {
        binding.tvForgotPassword.setOnClickListener{
            showForgotPasswordDialog()
        }
    }

    private fun setupRegisterClickAction() {
        binding.tvRegister.setOnClickListener{
            gotoRegistration()
        }
    }



    private fun setupLoginClickAction() {
        binding.loginBtn.setOnClickListener {
            onLoginClick()
        }
    }

    private fun onLoginClick() {
        val userId = binding.etUserIdCore.text.toString()
        val password = binding.etPasswordCore.text.toString()

        when{
            userId.isEmpty->{
                warn(getString(R.string.please_put_a_eamil_or_mobile))
                return
            }
            userId.isNotEmail->{
                warn(getString(R.string.please_put_a_valid_email_or_mobile))
                return
            }
            password.isEmpty->{
                warn(getString(R.string.please_enter_password))
                return
            }
            password.isNotPassword->{
                warn(getString(R.string.wrong_password_pattern))
                return
            }
        }
        doLogin(userId,password)
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
                    response.isError->response.errorMessage.toastLong(this@LoginActivity)
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
                                            loginResponse.message.toast(this@LoginActivity)
                                        }
                                    }
                                    else->{
                                        getString(R.string.something_went_wrong).toast(this@LoginActivity)
                                    }
                                }
                            }
                            else->{
                                getString(R.string.something_went_wrong).toast(this@LoginActivity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.message?.toast(this@LoginActivity)
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
                    response.isError->response.errorMessage.toastLong(this@LoginActivity)
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
                                            loginResponse.message.toast(this@LoginActivity)
                                        }
                                    }
                                    else->{
                                        getString(R.string.something_went_wrong).toast(this@LoginActivity)
                                    }
                                }
                            }
                            else->{
                                getString(R.string.something_went_wrong).toast(this@LoginActivity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                getString(R.string.something_went_wrong).toast(this@LoginActivity)
                e.message?.log("facebook_bug")
            }
            wait = false
        }

    }

    private fun warn(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun doLogin(userId: String, pass: String) {
        lifecycleScope.launch {
            wait = true
            try {
                val response = repository.getUser(userId,pass).resp
                when{
                    response.isError->response.errorMessage.toastLong(this@LoginActivity)
                    else->{
                        when{
                            response.isSuccess->{
                                val userResponse = response.body
                                when{
                                    userResponse!=null->{
                                        if(
                                            userResponse.success
                                            &&userResponse.user.user_id!=null
                                            &&userResponse.user.mobile!=null
                                        ){
                                            val uid = userResponse.user.user_id
                                            repository.userUid = uid
                                            repository.loginDone = true
                                            if(userResponse.user.mobile.isNotEmpty
                                                &&userResponse.user.mobile.isMobile
                                                &&userResponse.user.mobile_verified=="1"
                                            ){
                                                gotoHome()
                                                return@launch
                                            }
                                            else{
                                                gotoAddMobile(userResponse.user.mobile)
                                            }
                                        }
                                        else{
                                            userResponse.message.toast(this@LoginActivity)
                                        }
                                    }
                                    else->{
                                        getString(R.string.something_went_wrong).toast(this@LoginActivity)
                                    }
                                }
                            }
                            else->{
                                getString(R.string.something_went_wrong).toast(this@LoginActivity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                getString(R.string.something_went_wrong).toast(this@LoginActivity)
            }
            wait = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    private fun showForgotPasswordDialog() {
        if(forgotPasswordDialogShown){
            return
        }
        forgotPasswordDialogShown = true
        val bottomSheetDialog = RoundBottomSheet(this){
            forgotPasswordDialogShown = false
        }

        val binding = ForgotPasswordLayoutBinding.inflate(layoutInflater)
        val root = binding.root
        bottomSheetDialog.setContentView(root)

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.btSendOtp.setOnClickListener {
            val mobile = binding.etUserIdForgot.text?.string?:""
            if(mobile.isEmpty){
                R.string.please_put_a_mobile_number.string.toast(this)
                return@setOnClickListener
            }
            if(mobile.isNotMobile){
                R.string.please_put_a_valid_mobile_number.string.toast(this)
                return@setOnClickListener
            }
            lifecycleScope.launch {
                wait = true
                binding.etUserIdForgot.clearFocus()
                processApi {
                    repository.requestPasswordResetCode(mobile).resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.EXCEPTION -> {
                            exception?.message?.toast(this@LoginActivity)
                        }

                        ApiDispositionStatus.ERROR -> {
                            error?.toast(this@LoginActivity)
                        }

                        ApiDispositionStatus.FAILED,
                        ApiDispositionStatus.BODY_NULL,
                        ApiDispositionStatus.UNKNOWN -> {
                            R.string.something_went_wrong.string.toast(this@LoginActivity)
                        }
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    message.toast(this@LoginActivity)
                                    bottomSheetDialog.dismiss()
                                }
                                else{
                                    message.toast(this@LoginActivity)
                                    binding.etUserIdForgot.setText("")
                                    openKeyboard(binding.etUserIdForgot)
                                }
                            }
                        }
                    }
                }
                wait = false
            }
        }

        bottomSheetDialog.show()
    }
}




