package com.hellomydoc.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.data.AppointmentSummary
import com.hellomydoc.data.appointment_booking_doctor_response.Doctor
import com.hellomydoc.data.members.Member
import com.hellomydoc.views.DoctorSelectionView
import kotlinx.coroutines.launch


class AddPatientDetailsActivity : AbstractActivity() {
    private var selectedMember: Member? = null
    private val membersLoading = mutableStateOf(false)
    private val members = mutableStateListOf<Member>()
    private val membersVisible = mutableStateOf(false)
    private var doctorList: List<Doctor>? = null
    private var alv_doctors: AnyListView? = null
    private var v_bar_above_doctors: View? = null
    private var btn_continue: Button? = null
    private var et_name_core: EditText? = null
    private var et_age_core: EditText? = null
    private var rg_gender: RadioGroup? = null
    private var et_symptoms: EditText? = null
    private var iv_back: ImageView? = null
    private var iv_drop_down: ImageView? = null
    private var cv_members: ComposeView? = null
    var doctor: Doctor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient_details2)
        cv_members = findViewById(R.id.cv_members)
        iv_back = findViewById(R.id.iv_back)
        iv_drop_down = findViewById(R.id.iv_drop_down)
        alv_doctors = findViewById(R.id.alv_doctors)
        btn_continue = findViewById(R.id.btn_continue)
        et_name_core = findViewById(R.id.et_name_core)
        et_age_core = findViewById(R.id.et_age_core)
        rg_gender = findViewById(R.id.rg_gender)
        et_symptoms = findViewById(R.id.et_symptoms)
        v_bar_above_doctors = findViewById(R.id.v_bar_above_doctors)
        if(savedInstanceState==null){
            fetchMembers()
        }

        cv_members?.setContent {
            if(membersVisible.value){
                Dialog(
                    onDismissRequest = {
                        membersVisible.value = false
                    }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)

                    ){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ){
                            Text(
                                "Members",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Divider(
                                color = Color.LightGray,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            if(membersLoading.value){
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){
                                    CircularProgressIndicator(
                                        color = Color.Red,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            else{
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    items(members){
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp).clickable {
                                                    onMemberSelect(it)
                                                    membersVisible.value = false
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            AsyncImage(
                                                model = it.image,
                                                contentDescription = "Image",
                                                modifier = Modifier.size(48.dp).clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                getMemberDisplayNameValue(it),
                                                color = Color.Black,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        iv_drop_down?.setOnClickListener {
            if(selectedMember==null){
                onMembersDroopDownClick()
            }
            else{
                fillPatientDetailsFromMember(null)
            }
        }

        iv_back?.setOnClickListener {
            finish()
        }

        btn_continue?.setOnClickListener {
            val name = et_name_core?.text?.string?:""
            if(name.isEmpty){
                R.string.please_put_patient_name.string.toast(this)
                return@setOnClickListener
            }
            val age = et_age_core?.text?.string?:""
            if(age.isEmpty){
                R.string.please_put_patient_age.string.toast(this)
                return@setOnClickListener
            }
            val gender = rg_gender?.checkedRadioButtonId?:0
            if(gender==-1){
                R.string.please_select_gender.string.toast(this)
                return@setOnClickListener
            }
            val symptoms = et_symptoms?.text?.string?:""
            if(symptoms.isEmpty){
                R.string.please_put_symptoms.string.toast(this)
                return@setOnClickListener
            }
            if(doctor==null){
                R.string.please_select_a_doctor.string.toast(this)
                return@setOnClickListener
            }
            var genderString = when(gender){
                R.id.rb_male->Constants.MALE
                R.id.rb_female->Constants.FEMALE
                else -> Constants.OTHER
            }
            continueOnPatientDetails(name,age,genderString,symptoms, doctor!!)
        }

        if(savedInstanceState==null){
            lifecycleScope.launch {
                wait = true
                processApi {
                    repository.getDoctorsForAppointmentBooking("1", "Video", "Pediatrician").resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    doctorList = doctors
                                    showDoctorList()
                                }
                                else{
                                    message.toast(this@AddPatientDetailsActivity)
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(this@AddPatientDetailsActivity)
                        }
                    }
                }
                wait = false
            }
        }
    }

    private fun getMemberDisplayNameValue(member: Member): String {
        if(member.user_id== repository.userUid){
            return "MySelf"
        }
        else{
            return "${member.name}(${member.genderShort}-${member.age})"
        }
    }

    private fun onMemberSelect(member: Member) {
        fillPatientDetailsFromMember(member)
    }


    private fun fillPatientDetailsFromMember(member: Member?) {
        if(member==null){
            et_name_core?.setText("")
            et_name_core?.isEnabled = true
            et_age_core?.setText("")
            et_age_core?.isEnabled = true
            rg_gender?.clearCheck()
            findViewById<View>(R.id.rb_male).isEnabled = true
            findViewById<View>(R.id.rb_female).isEnabled = true
            findViewById<View>(R.id.rb_other).isEnabled = true
            rg_gender?.isEnabled = true
            iv_drop_down?.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            selectedMember = null
        }
        else{
            et_name_core?.setText(member.name)
            et_name_core?.isEnabled = false
            et_age_core?.setText(member.age.toString())
            et_age_core?.isEnabled = false

            val checkableId = when(member.genderShort){
                "M"->R.id.rb_male
                "F"->R.id.rb_female
                else->R.id.rb_other
            }
            rg_gender?.check(checkableId)
            findViewById<View>(R.id.rb_male).isEnabled = false
            findViewById<View>(R.id.rb_female).isEnabled = false
            findViewById<View>(R.id.rb_other).isEnabled = false
            rg_gender?.isEnabled = false
            iv_drop_down?.setImageResource(R.drawable.ic_baseline_close_24)
            selectedMember = member
        }
    }

    private fun fetchMembers() {
        membersLoading.value = true
        lifecycleScope.launch {
            try {
                val r = repository.fetchMembers().resp
                membersLoading.value = false
                if(r.isSuccess){
                    val mr = r.body?:return@launch
                    if(mr.success){
                        members.clear()
                        members.addAll(mr.members)
                    }
                }
            } catch (e: Exception) {
                membersLoading.value = false
            }
        }
    }

    private fun onMembersDroopDownClick() {
        membersVisible.value = true
    }

    private fun continueOnPatientDetails(
        name: String,
        age: String,
        gender: String,
        symptoms: String,
        doctor: Doctor
    ) {
        if(selectedMember!=null){
            repository.setPatientId(selectedMember?.user_id?:return)
            repository.setPatientSource("id")
        }
        else{
            repository.setPatientName(name)
            repository.setPatientAge(age.toInt())
            repository.setPatientGender(gender)
            repository.setPatientSource("raw")
        }

        repository.setPatientSymptoms(symptoms)
        repository.setDoctor(doctor)
        val price = repository.getAppointmentPrice()
        if(price>0){
            navi().target(AddPaymentDetails::class.java).go()
        }
        else{
            val appointmentSummary = AppointmentSummary(
                userId = repository.userUid,
                speciality = repository.getAppointmentSpeciality(),
                type = repository.getAppointmentType().name,
                price = price,
                slot = repository.getSelectedSlot(),
                patientName = repository.getPatientName(),
                patientAge = repository.getPatientAge(),
                gender = gender,
                symptoms = symptoms,
                doctor = doctor.id,
                paymentMethod = "",
                patientId = repository.getPatientId()
            )


            lifecycleScope.launch {
                wait = true
                processApi {
                    repository.appointmentFinalize(appointmentSummary).resp
                }.apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    repository.setLastAppointmentFinalizedId(appointmentId)
                                    navi().back()
                                }
                                else{
                                    message.toast(this@AddPatientDetailsActivity)
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(this@AddPatientDetailsActivity)
                        }
                    }
                }
                wait = false
            }
        }
    }


    var prevSelectedDoctorPos = -1
    fun showDoctorList() {
        alv_doctors?.configure(
            AnyListView.Configurator(
                onScrollPx = {dx,dy->
                    val a = dy.toFloat().coerceAtMost(17f).map(0f,17f,0f,1f)
                    v_bar_above_doctors?.alpha = a
                },
                itemType = {pos->pos},
                state = AnyListView.STATE.DATA,
                itemCount = {doctorList?.size?:0},
                onView = {pos,view->
                    ((view as? ViewGroup)?.findViewWithTag("doctor_view") as? DoctorSelectionView)?.apply {
                        setData(doctorList?.getOrNull(pos))
                        onCheckedChanged = {
                            Log.d("selection_bug","$pos $it");
                            if(it){
                                if(pos!=prevSelectedDoctorPos&&prevSelectedDoctorPos!=-1){
                                    doctorList?.getOrNull(prevSelectedDoctorPos)?.selected = false
                                    alv_doctors?.notifyItemChanged(prevSelectedDoctorPos)
                                }
                                doctorList?.getOrNull(pos)?.selected = it
                            }
                            doctor = if(it){
                                prevSelectedDoctorPos = pos
                                doctorList?.getOrNull(pos)
                            } else{
                                //prevSelectedDoctorPos = -1
                                null
                            }
                        }
                    }
                },
                itemView = {pos->
                    DoctorSelectionView(this).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            setMargins(0,20,0,30)
                            tag = "doctor_view"
                        }
                    }
                }
            )
        )
    }
}