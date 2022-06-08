package com.hellomydoc.activities

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import com.hellomydoc.*
import com.hellomydoc.databinding.ActivityVerifyOtpBinding
import kotlinx.coroutines.launch

class VerifyOtpActivity : AbstractActivity() {
    private var currentOtp = ""
    private var mobile = ""
    private lateinit var binding: ActivityVerifyOtpBinding
    private var keyboardTriggerBehavior: KeyboardTriggerBehavior? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        onCreatedActions()

        if(savedInstanceState==null){
            requestOTP()
        }

    }

    private fun requestOTP() {
        mobile = intent?.getStringExtra(Constants.MOBILE_KEY)?:""
        if(mobile.isNotEmpty){
            lifecycleScope.launch {
                wait = true
                try {
                    var response = repository.requestOTP(mobile).resp
                    ////////////////////////////
                    when{
                        response.isError->response.errorMessage.toastLong(this@VerifyOtpActivity)
                        else->{
                            when{
                                response.isSuccess->{
                                    val otpResponse = response.body
                                    when{
                                        otpResponse!=null->{
                                            if(otpResponse.success){
                                                currentOtp = otpResponse.otp
                                            }
                                        }
                                        else->{
                                            getString(R.string.something_went_wrong).toast(this@VerifyOtpActivity)
                                        }
                                    }
                                }
                                else->{
                                    getString(R.string.something_went_wrong).toast(this@VerifyOtpActivity)
                                }
                            }
                        }
                    }
                    ////////////////////////////
                } catch (e: Exception) {
                    e.message?.toast(this@VerifyOtpActivity)
                }
                wait = false
                binding.firstPinView.requestFocus()
            }
        }
    }

    private fun onCreatedActions() {
        setupBackButton()
        setupVerifyButton()
        trackKeyboardState()
    }

    private fun setupVerifyButton() {
        binding.sendOtpBtnVerify.setOnClickListener {
            val inputOtp = binding.firstPinView.text.toString()
            when{
                inputOtp.isEmpty->{
                    getString(R.string.please_put_the_otp).toast(this)
                }
                inputOtp.md5!=currentOtp->{
                    getString(R.string.otp_does_not_match).toast(this)
                }
                inputOtp.md5==currentOtp->{
                    lifecycleScope.launch {
                        val user_id = repository.userUid
                        if(user_id.isEmpty){
                            getString(R.string.user_id_seems_to_be_empty).toast(this@VerifyOtpActivity)
                            return@launch
                        }
                        try {
                            var response = repository.setMobileVerified(mobile, user_id).resp
                            ////////////////////////////
                            when{
                                response.isError->response.errorMessage.toastLong(this@VerifyOtpActivity)
                                else->{
                                    when{
                                        response.isSuccess->{
                                            val mobileVerifiedSetResponse = response.body
                                            when{
                                                mobileVerifiedSetResponse!=null->{
                                                    if(mobileVerifiedSetResponse.success){
                                                        repository.loginDone = true
                                                        gotoHome()
                                                    }
                                                    else{
                                                        mobileVerifiedSetResponse.message.toast(this@VerifyOtpActivity)
                                                    }
                                                }
                                                else->{
                                                    getString(R.string.something_went_wrong).toast(this@VerifyOtpActivity)
                                                }
                                            }
                                        }
                                        else->{
                                            getString(R.string.something_went_wrong).toast(this@VerifyOtpActivity)
                                        }
                                    }
                                }
                            }
                            ////////////////////////////
                        } catch (e: Exception) {
                            e.message?.toast(this@VerifyOtpActivity)
                        }
                    }

                }
                else -> {}
            }
        }
    }

    private fun trackKeyboardState() {
        keyboardTriggerBehavior = KeyboardTriggerBehavior(this,100).apply {
            observe(this@VerifyOtpActivity){
                when (it) {
                    KeyboardTriggerBehavior.Status.OPEN -> {
                        val lp = binding.filler.layoutParams
                        lp.height = 300.pxToDp
                        binding.filler.layoutParams = lp
                        binding.filler.requestLayout()
                        binding.scvRoot.fullScroll(View.FOCUS_DOWN)
                    }
                    KeyboardTriggerBehavior.Status.CLOSED -> {
                        val lp = binding.filler.layoutParams
                        lp.height = 0
                        binding.filler.layoutParams = lp
                        binding.filler.requestLayout()
                    }
                }
            }
        }
    }

    private fun setupBackButton() {
        binding.addOtpBackBtn.setOnClickListener{
            navi()
                .target(AddMobileNumberActivity::class.java)
                .back()
        }
    }
}

