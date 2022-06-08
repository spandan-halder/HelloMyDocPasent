package com.hellomydoc.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hellomydoc.*
import com.hellomydoc.Tyfo.overrideFont
import com.xeinebiu.suspend.dialogs.SuspendAlertDialog
import kotlinx.coroutines.launch


open class AbstractActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTypeFace()
    }

    fun navi(): Navi {
        return Navi(this)
    }

    val activityResultCallbacks =
        ArrayList<((requestCode: Int, resultCode: Int, data: Intent?) -> Unit)?>()

    fun addActivityResultCallback(callback: ((requestCode: Int, resultCode: Int, data: Intent?) -> Unit)?) {
        activityResultCallbacks.add(callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (i in activityResultCallbacks.indices) {
            var callback = activityResultCallbacks.removeAt(i)
            if (callback != null) {
                callback(requestCode, resultCode, data)
            }
        }

    }

    private var _wait = false
    var wait: Boolean
        get() = _wait
        set(value){
            _wait = value
            if(value){
                showWaiting()
            }
            else{
                hideWaiting()
            }
        }

    private var loaderDialog: Dialog? = null
    private fun hideWaiting() {
        loaderDialog?.dismiss()
    }

    private fun showWaiting() {
        loaderDialog = Dialog(this)
        /////////////////////////////
        loaderDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.loader_dialog_layout)
            window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                val wlp = attributes
                wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
                attributes = wlp
                setLayout(screenWidth, screenHeight)
            }
        }
        /////////////////////////////
        loaderDialog?.show()
    }

    protected fun gotoAddMobile(mobile: String) {
        navi()
            .target(AddMobileNumberActivity::class.java)
            .add(Constants.MOBILE_KEY,mobile)
            ?.go()
    }

    protected fun gotoHome() {
        navi()
            .target(SplashActivity::class.java)
            .go()
    }

    protected fun gotoRegistration() {
        navi()
            .target(RegisterActivity::class.java)
            .go()
    }

    protected fun goBackToLogin() {
        navi().target(LoginActivity::class.java).back()
    }

    protected fun alert(message: String){
        lifecycleScope.launch {
            SuspendAlertDialog.alert(R.string.ok.string) {
                MaterialAlertDialogBuilder(this@AbstractActivity)
                    .setTitle(R.string.app_name.string)
                    .setMessage(R.string.password_pattern_warning.string)
            }
        }
    }

    protected fun openKeyboard(view: View){
        view.requestFocus()
        view.postDelayed({
            val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(view, 0)
        },200)
    }

    override fun onDestroy() {
        loaderDialog?.dismiss()
        loaderDialog = null
        super.onDestroy()
    }

    private fun setTypeFace() {
        val font = "fonts/Roboto-Regular.ttf"
        overrideFont(this, "DEFAULT", font)
        overrideFont(this, "MONOSPACE", font)
        overrideFont(this, "SERIF", font)
        overrideFont(this, "SANS_SERIF", font)
    }
}