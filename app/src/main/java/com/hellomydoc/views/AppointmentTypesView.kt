package com.hellomydoc.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import com.hellomydoc.R
import com.hellomydoc.data.AppointmentPrices

class AppointmentTypesView : LinearLayoutCompat {
    var type:AppointmentTypeView.APPOINTMENT_TYPE = AppointmentTypeView.APPOINTMENT_TYPE.NONE
        private set(value){
            field = value
        }
    private val layoutId = R.layout.appointment_types_view_layout

    private var atvVideo: AppointmentTypeView? = null
    private var atvChat: AppointmentTypeView? = null
    private var atvCall: AppointmentTypeView? = null

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
        context!!, attrs, defStyleAttr
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
        atvVideo = findViewById(R.id.atv_video)
        atvChat = findViewById(R.id.atv_chat)
        atvCall = findViewById(R.id.atv_call)

        atvVideo?.setAppointmentType(AppointmentTypeView.APPOINTMENT_TYPE.VIDEO)
        atvChat?.setAppointmentType(AppointmentTypeView.APPOINTMENT_TYPE.CHAT)
        atvCall?.setAppointmentType(AppointmentTypeView.APPOINTMENT_TYPE.VOICE)

        atvVideo?.setOnClickListener {
            onSelect(AppointmentTypeView.APPOINTMENT_TYPE.VIDEO)
        }

        atvChat?.setOnClickListener {
            onSelect(AppointmentTypeView.APPOINTMENT_TYPE.CHAT)
        }

        atvCall?.setOnClickListener {
            onSelect(AppointmentTypeView.APPOINTMENT_TYPE.VOICE)
        }
    }

    private fun onSelect(type: AppointmentTypeView.APPOINTMENT_TYPE,byUser: Boolean = true) {
        this.type = type
        onCoreSelect(type,byUser)
    }

    private fun onCoreSelect(type: AppointmentTypeView.APPOINTMENT_TYPE, byUser: Boolean) {
        when(type){
            AppointmentTypeView.APPOINTMENT_TYPE.VIDEO -> {
                atvVideo?.setStatus(true)
                atvChat?.setStatus(false)
                atvCall?.setStatus(false)
            }
            AppointmentTypeView.APPOINTMENT_TYPE.CHAT -> {
                atvVideo?.setStatus(false)
                atvChat?.setStatus(true)
                atvCall?.setStatus(false)
            }
            AppointmentTypeView.APPOINTMENT_TYPE.VOICE -> {
                atvVideo?.setStatus(false)
                atvChat?.setStatus(false)
                atvCall?.setStatus(true)
            }
        }
        notifySelectionChanged(type,byUser)
    }

    var onSelectionChanged: ((AppointmentTypeView.APPOINTMENT_TYPE,Boolean)->Unit)? = null

    private fun notifySelectionChanged(type: AppointmentTypeView.APPOINTMENT_TYPE, byUser: Boolean) {
        onSelectionChanged?.invoke(type,byUser)
    }

    fun selectType(type: AppointmentTypeView.APPOINTMENT_TYPE){
        onSelect(type,false)
    }

    fun setPrices(prices: AppointmentPrices) {
        atvVideo?.setPrice(prices.videoAppointmentPrice)
        atvChat?.setPrice(prices.chatAppointmentPrice)
        atvCall?.setPrice(prices.callAppointmentPrice)
    }
}