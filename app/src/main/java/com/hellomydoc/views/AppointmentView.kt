package com.hellomydoc.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.hellomydoc.R
import com.hellomydoc.data.AppointmentData
import com.hellomydoc.databinding.AppointmentViewBinding
import com.hellomydoc.setImage
import com.hellomydoc.string

class AppointmentView : LinearLayout {

    private var wholeViewClickListener: (()->Unit)? = null
    private var typeViewClickListener: (()->Unit)? = null
    private lateinit var binding: AppointmentViewBinding

    //declare views here
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        binding = AppointmentViewBinding.inflate(LayoutInflater.from(context))
        binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
        addView(binding.root)
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
        data = null
    }

    private var _data: AppointmentData? = null

    var data: AppointmentData?
        get(){
            return _data
        }
        set(value){
            _data = value
            onDataChanged()
        }

    private fun onDataChanged() {
        if(_data!=null){
            binding.ivImage.setImage(_data?.image)
            binding.tvDate.text = _data?.date
            binding.tvTime.text = _data?.time
            binding.tvDoctor.text = _data?.doctorName
            binding.tvCategory.text = _data?.category
            when(data?.type?.uppercase()){
                AppointmentTypeView.APPOINTMENT_TYPE.CHAT.name->binding.ivChatType.setImageResource(R.drawable.ic_chat_svgrepo_com)
                AppointmentTypeView.APPOINTMENT_TYPE.VIDEO.name->binding.ivChatType.setImageResource(R.drawable.ic_video_svgrepo_com)
                AppointmentTypeView.APPOINTMENT_TYPE.VOICE.name->binding.ivChatType.setImageResource(R.drawable.ic_phone_call_svgrepo_com)
            }
        }
        else{
            binding.ivImage.setImageResource(R.drawable.ic_doctor_svgrepo_com)
            binding.tvDate.text = R.string.not_found.string
            binding.tvTime.text = R.string.not_found.string
            binding.tvDoctor.text = R.string.not_found.string
            binding.tvCategory.text = R.string.not_found.string
            binding.ivChatType.setImageBitmap(null)
        }
    }

    fun setCallback(
        wholeViewClickListener: (()->Unit)? = null,
        typeViewClickListener: (()->Unit)? = null
    ){
        this.wholeViewClickListener = wholeViewClickListener
        this.typeViewClickListener = typeViewClickListener

        this.setOnClickListener {
            wholeViewClickListener?.invoke()
        }

        binding.ivChatType.setOnClickListener {
            typeViewClickListener?.invoke()
        }
    }
}