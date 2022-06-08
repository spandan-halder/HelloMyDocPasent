package com.hellomydoc.activities

import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.hellomydoc.*
import com.hellomydoc.databinding.ActivityAddMobileNumberBinding
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

class AddMobileNumberActivity : AbstractActivity() {
    private lateinit var binding: ActivityAddMobileNumberBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMobileNumberBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        onCreatedAction()
    }

    private fun onCreatedAction() {
        binding.etMobile.setText(intent?.getStringExtra(Constants.MOBILE_KEY)?:"")
        setupTermsAndServices()
        setupOtpButton()
        setupBackButton()
    }

    private fun setupBackButton() {
        binding.addNumBackBtn.setOnClickListener{
            navi().target(LoginActivity::class.java).back()
        }
    }

    private fun setupOtpButton() {
        binding.btNext.setOnClickListener {
            val entry = binding.etMobile.text.toString()
            when{
                entry.isEmpty->{
                    getString(R.string.please_put_a_mobile_number).toast(this)
                    return@setOnClickListener
                }
                entry.isNotMobile->{
                    getString(R.string.please_put_a_valid_mobile_number).toast(this)
                    return@setOnClickListener
                }
            }
            navi()
                .target(VerifyOtpActivity::class.java)
                .add(Constants.MOBILE_KEY,entry)
                ?.go()
        }
    }

    private fun setupTermsAndServices() {
        binding.tvTermsOfService.movementMethod = LinkMovementMethod()
        binding.tvTermsOfService.text = Spanner(getString(R.string.add_number_terms_p1))
            .append(getString(R.string.add_number_terms_p2),Spans.click {
                navi().target(TermsOfServiceActivity::class.java).go()
            })
            .append(getString(R.string.add_number_terms_p3))
            .append(getString(R.string.add_number_terms_p4), Spans.click {
                navi().target(PrivacyPolicyActivity::class.java).go()
            })
            .append(getString(R.string.add_number_terms_p5))
    }
}