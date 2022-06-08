package com.hellomydoc.activities

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.hellomydoc.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        delayAndGo()
    }

    private fun delayAndGo() {
        lifecycleScope.launch {
            delay(1000)
            processApi {
                repository.checkAgeAndGender().resp
            }.apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            repository.setAgeAndGenderOk(success)
                        }
                        goToPage()
                    }
                    else->{
                        repository.setAgeAndGenderOk(false)
                        runOnUiThread {
                            delayAndGo()
                        }
                    }
                }
            }
        }
    }

    private fun goToPage() {
        if (!repository.introSeen){
            navi()
                .target(IntroActivity::class.java)
                .go()
        }
        else if(repository.loginDone){
            navi()
                .target(HomeActivity::class.java)
                .go()
        }
        else{
            navi()
                .target(LoginActivity::class.java)
                .go()
        }
    }
}