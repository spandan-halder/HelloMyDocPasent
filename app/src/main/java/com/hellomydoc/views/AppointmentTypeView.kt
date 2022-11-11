package com.hellomydoc.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hellomydoc.R
import com.hellomydoc.drawable
import com.hellomydoc.string

class AppointmentTypeView : ConstraintLayout {

    private var tvTitle: TextView? = null
    private var ivIcon: ImageView? = null
    private var tvPrice: TextView? = null
    private var clAppointmentTypeViewRoot: ConstraintLayout? = null

    private val layoutId = R.layout.appointment_type_view_layout
    //declare views here
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
        init()
    }

    private fun init() {
        inflate(context, layoutId, this)
        setupViews()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        //bundle.putInt("stuff", this.stuff); // ... save stuff
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) // implicit null check
        {
            //this.stuff = bundle.getInt("stuff"); // ... load stuff
            state = state.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }

    private fun setupViews() {
        tvTitle = findViewById(R.id.tv_title)
        ivIcon = findViewById(R.id.iv_icon)
        tvPrice = findViewById(R.id.tv_price)
        clAppointmentTypeViewRoot = findViewById(R.id.cl_appointment_type_view_root)
    }

    var checked: Boolean = false
        set(value) {
            field = value
            updateAsCheckedStatus()
        }

    enum class APPOINTMENT_TYPE{
        VIDEO,
        CHAT,
        VOICE,
        NONE;

        companion object{
            fun fromString(name: String):APPOINTMENT_TYPE{
                return when(name){
                    "VIDEO"->VIDEO
                    "CHAT"->CHAT
                    "VOICE"->VOICE
                    else->NONE
                }
            }
        }
    }

    var type: APPOINTMENT_TYPE = APPOINTMENT_TYPE.VIDEO
        set(value) {
            field = value
            updateAsType()
        }

    private fun updateAsType() {
        when(type){
            APPOINTMENT_TYPE.VIDEO -> {
                tvTitle?.text =  R.string.video_consultation.string
                ivIcon?.setImageResource(R.drawable.ic_videocam_black_24dp)
                Log.d("Testing1", "ButtonClick")
            }
            APPOINTMENT_TYPE.CHAT -> {
                tvTitle?.text = R.string.chat_consultation.string
                ivIcon?.setImageResource(R.drawable.ic_question_answer_black_24dp)
            }
            APPOINTMENT_TYPE.VOICE -> {
                tvTitle?.text = R.string.call_consultation.string
                ivIcon?.setImageResource(R.drawable.ic_call_black_24dp)
            }
        }
    }

    private fun updateAsCheckedStatus() {
        when(checked){
            true->{
                clAppointmentTypeViewRoot?.background = R.drawable.appointment_type_background.drawable()
                ivIcon?.background = R.drawable.white_circle.drawable()
            }
            false->{
                clAppointmentTypeViewRoot?.background = R.drawable.appointment_type_background_normal.drawable()
                ivIcon?.background = R.drawable.light_red_circle.drawable()
            }
        }
    }

    fun setAppointmentType(type: AppointmentTypeView.APPOINTMENT_TYPE){
        this.type = type
    }

    fun setStatus(status: Boolean){
        this.checked = status
    }

    fun setPrice(price: Float) {
        val currency = R.string.currency.string
        tvPrice?.text = "$price $currency"
    }
}