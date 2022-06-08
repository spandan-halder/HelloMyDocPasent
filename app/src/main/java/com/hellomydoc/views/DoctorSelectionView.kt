package com.hellomydoc.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hellomydoc.R
import com.hellomydoc.data.appointment_booking_doctor_response.Doctor
import com.hellomydoc.drawable
import com.hellomydoc.setImage
import de.hdodenhof.circleimageview.CircleImageView

class DoctorSelectionView : ConstraintLayout {

    private var civ_doctor: CircleImageView? = null
    private var tv_doctor_name: TextView? = null
    private var tv_speciality_and_experience: TextView? = null
    private var cb_check: CheckBox? = null
    private var cl_root: ViewGroup? = null
    private val onCheckChangedListener =
        CompoundButton.OnCheckedChangeListener { p0, p1 ->
            if(p1){
                cl_root?.background = R.drawable.appointment_type_background.drawable()
            } else{
                cl_root?.background = R.drawable.appointment_type_background_normal.drawable()
            }
            onCheckedChanged?.invoke(p1)
        }

    constructor(context: Context) : super(context) {init()}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init()}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init(){
        inflate(context, R.layout.doctor_selection_view_layout, this)
        setupViews()
    }

    private fun setupViews() {
        civ_doctor = findViewById(R.id.civ_doctor)
        tv_doctor_name = findViewById(R.id.tv_doctor_name)
        tv_speciality_and_experience = findViewById(R.id.tv_speciality_and_experience)
        cb_check = findViewById(R.id.cb_check)
        cl_root = findViewById(R.id.cl_root)

        cl_root?.setOnClickListener {
            cb_check?.performClick()
        }

        cb_check?.setOnCheckedChangeListener(onCheckChangedListener)
    }

    var onCheckedChanged: ((Boolean)->Unit)? = null

    fun setData(doctor: Doctor?){
        doctor?.apply {
            civ_doctor?.setImage(image)
            tv_doctor_name?.text = name
            tv_speciality_and_experience?.text = "$speciality • $experience years of experience"
            cb_check?.setOnCheckedChangeListener(null)
            cb_check?.isChecked = selected
            if(selected){
                cl_root?.background = R.drawable.appointment_type_background.drawable()
            } else{
                cl_root?.background = R.drawable.appointment_type_background_normal.drawable()
            }
            cb_check?.setOnCheckedChangeListener(onCheckChangedListener)
        }
    }
}