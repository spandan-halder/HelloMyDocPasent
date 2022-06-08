package com.hellomydoc.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


class  CustomDialog private constructor(val context: Context, @StyleRes val theme: Int? = null) {
    private var _dialog: Dialog? = null
    init {
        _dialog = if(theme!=null){
            Dialog(context,theme)
        }
        else{
            Dialog(context)
        }
    }

    fun show(){
        _dialog?.show()
    }

    fun layout(@LayoutRes layout: Int): CustomDialog{
        _dialog?.setContentView(layout)
        return this
    }

    fun layout(view: View):CustomDialog{
        _dialog?.setContentView(view)
        return this
    }

    fun dim(dimAmount: Float = 1f): CustomDialog{
        val lp = _dialog?.window?.attributes
        lp?.dimAmount = dimAmount

        _dialog?.window?.attributes = lp

        //dialog?.window?.setDimAmount(dimAmount)
        return this
    }

    fun noDim(): CustomDialog{
        _dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return this
    }

    fun cancelable(yes: Boolean = true): CustomDialog{
        _dialog?.setCancelable(false)
        return this
    }

    fun size(width: Int = ViewGroup.LayoutParams.WRAP_CONTENT, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT): CustomDialog{
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(_dialog?.window?.attributes)
        lp.width = width
        lp.height = height
        _dialog?.window?.attributes = lp
        return this
    }

    fun noBackground(): CustomDialog{
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return this
    }

    val dialog: Dialog?
        get(){
            return _dialog
        }


    @OptIn(ExperimentalContracts::class)
    inline fun with(block: Dialog.() -> Unit): CustomDialog {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        dialog?.let {
            block(it)
        }
        return this
    }

    fun dismiss(){
        _dialog?.dismiss()
    }

    companion object{
        fun create(context: Context, @StyleRes theme: Int? = null): CustomDialog{
            return CustomDialog(context,theme)
        }
    }
}