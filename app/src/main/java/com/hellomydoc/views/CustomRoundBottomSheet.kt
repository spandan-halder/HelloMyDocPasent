package com.hellomydoc.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hellomydoc.dialogs.RoundBottomSheet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class CustomRoundBottomSheet private constructor(private var context: Context){
    private var _roundBottomSheet: RoundBottomSheet? = null
    init {
        _roundBottomSheet = RoundBottomSheet(context){

        }
    }

    fun layout(@LayoutRes layout: Int): CustomRoundBottomSheet{
        _roundBottomSheet?.setContentView(layout)
        return this
    }
    fun layout(view: View): CustomRoundBottomSheet{
        _roundBottomSheet?.setContentView(view)
        return this
    }
    fun layout(content: @Composable () -> Unit): CustomRoundBottomSheet{
        layout(
            ComposeView(context)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setContent { content() }
                }
        )
        return this
    }
    val roundBottomSheet: RoundBottomSheet?
    get(){
        return _roundBottomSheet
    }
    @OptIn(ExperimentalContracts::class)
    inline fun with(block: RoundBottomSheet.() -> Unit): CustomRoundBottomSheet {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        roundBottomSheet?.let {
            block(it)
        }
        return this
    }
    fun show(){
        _roundBottomSheet?.expand()
        _roundBottomSheet?.show()
    }
    fun dismiss(){
        _roundBottomSheet?.dismiss()
    }
    companion object{
        fun create(context: Context): CustomRoundBottomSheet{
            return CustomRoundBottomSheet(context)
        }
    }
}

fun RoundBottomSheet.expand(){
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}

fun RoundBottomSheet.collapse(){
    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
}

fun RoundBottomSheet.halfExpanded(){
    behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
}

fun RoundBottomSheet.hidden(){
    behavior.state = BottomSheetBehavior.STATE_HIDDEN
}