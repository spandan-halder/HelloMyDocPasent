package com.hellomydoc.views

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.hellomydoc.color
import com.hellomydoc.dpToPx
import com.hellomydoc.drawable
import android.graphics.Typeface

import android.R
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat


class SlotChip : TextView {
    private val CHECKED_KEY = "checked"
    private var checked = false
    constructor(context: Context?) : super(context) {init()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {init()}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init(){
        setUpFont()
        updateAsState()
        isSaveEnabled = true
        setOnClickListener {
            checked = !checked
            updateAsState()
            notifyCheckChanged()
        }
    }

    var onCheckChanged: ((Boolean)->Unit)? = null
    private fun notifyCheckChanged() {
        onCheckChanged?.invoke(checked)
    }

    private fun setUpFont() {
        typeface = ResourcesCompat.getFont(context, com.hellomydoc.R.font.roboto_bold)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putBoolean(CHECKED_KEY, checked) // ... save stuff
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var state = state
        if (state is Bundle) // implicit null check
        {
            val bundle = state
            this.checked = bundle.getBoolean(CHECKED_KEY) // ... load stuff
            state = bundle.getParcelable("superState")
            updateAsState()
        }
        super.onRestoreInstanceState(state)
    }

    private fun updateAsState() {
        Log.d("check_bug_inner","$checked")
        background = if(checked){
            com.hellomydoc.R.drawable.slot_chip_checked.drawable()
        } else{
            com.hellomydoc.R.drawable.slot_chip_normal.drawable()
        }
        val padding = 12.dpToPx
        gravity = Gravity.CENTER
        setPadding(padding,padding,padding,padding)
        setTextColor(if(checked){
            com.hellomydoc.R.color.white.color} else{
            com.hellomydoc.R.color.red.color})
    }

    fun changeState(checked: Boolean){
        this.checked = checked
        updateAsState()
    }
}