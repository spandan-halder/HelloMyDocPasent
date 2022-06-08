package com.hellomydoc

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Pair
import android.view.View
import androidx.core.app.ActivityCompat
import com.hellomydoc.activities.AbstractActivity

class Navi(private val context: Context) {
    var intent: Intent? = null
    var finish = true
    fun target(activity: Class<*>?): Navi {
        intent = Intent(context, activity)
        return this
    }

    fun add(key: String?, value: String?): Navi? {
        return if (intent != null) {
            intent!!.putExtra(key, value)
            this
        } else {
            null
        }
    }

    fun add(key: String?, value: Int): Navi? {
        return if (intent != null) {
            intent!!.putExtra(key, value)
            this
        } else {
            null
        }
    }

    fun add(key: String?, value: Parcelable?): Navi? {
        return if (intent != null) {
            intent!!.putExtra(key, value)
            this
        } else {
            null
        }
    }

    fun finish(yes: Boolean): Navi? {
        return if (intent != null) {
            finish = yes
            this
        } else {
            null
        }
    }

    fun go() {
        if (intent != null) {
            context.startActivity(intent)
            (context as AbstractActivity).overridePendingTransition(
                R.anim.enter_anim,
                R.anim.exit_anim
            )
            if (finish) {
                context.finish()
            }
            intent = null
            finish = true
        }
    }

    fun back() {
        if (intent != null) {
            context.startActivity(intent)
            (context as AbstractActivity).overridePendingTransition(
                R.anim.close_enter,
                R.anim.close_exit
            )
            if (finish) {
                context.finish()
            }
            intent = null
            finish = true
        } else {
            (context as AbstractActivity).finish()
            context.overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
        }
    }

    fun shareGo(vararg sharedElements: Pair<View?, String?>?) {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as AbstractActivity,
                    *sharedElements
                )
                ActivityCompat.startActivity(context, intent!!, options.toBundle())
                intent = null
                finish = true
            } else {
                go()
            }
        }
    }

    fun shareBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (context as AbstractActivity).finishAfterTransition()
        } else {
            (context as AbstractActivity).finish()
        }
    }

    fun add(key: String?, value: Boolean): Navi? {
        return if (intent != null) {
            intent!!.putExtra(key, value)
            this
        } else {
            null
        }
    }

    fun bundle(bundle: Bundle?): Navi?{
        bundle?.let {
            intent?.putExtras(it)
        }
        return if(intent==null){
            null
        } else{
            this
        }
    }
}