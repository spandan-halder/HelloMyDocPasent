package com.hellomydoc.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.activities.HomeActivity
import com.hellomydoc.data.*
import com.hellomydoc.data.slots.DateSlot
import com.hellomydoc.databinding.AppointmentTypeDialogBinding
import com.hellomydoc.databinding.FragmentMyBookingsBinding
import com.hellomydoc.dialogs.RoundBottomSheet
import com.hellomydoc.views.AppointmentTypeView
import com.hellomydoc.views.DatesView
import com.hellomydoc.views.SlotsView
import com.hellomydoc.views.dateFormat
import kotlinx.coroutines.launch

class MyBookingsFragment : Fragment() {
    private var loading = false
    private var callback: HomeActivity.ChildCallback? = null
    private lateinit var binding: FragmentMyBookingsBinding
    private var bookings: List<AppointmentData> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBookingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAdd.setOnClickListener {
            addAppointment()
        }

        fetchAppointments()
    }

    private fun fetchAppointments() {
        loading = true
        showBookings()
        lifecycleScope.launch {
            processApi {
                repository.bookings().resp
            }
                .apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    this@MyBookingsFragment.bookings = appointments
                                    loading = false
                                    showBookings()
                                }
                            }
                        }
                        else->{
                            R.string.something_went_wrong.string.toast(requireContext())
                        }
                    }
                }
        }
    }

    private fun showBookings() {
        try {
            binding.cvList.setContent {
                if(bookings.size<1){
                    if(loading){
                        Box(modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center)
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(50.dp),
                                color = colorResource(id = R.color.red)
                            )
                        }
                    }
                    else{
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val image: Painter = painterResource(id = R.drawable.ic_history)
                                Image(
                                    painter = image,
                                    contentDescription = "",
                                    contentScale = ContentScale.Inside,
                                    modifier = Modifier.padding(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    R.string.no_bookings_yet.string,
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.W600
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        addAppointment()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.White,
                                        contentColor = Color.Black)
                                    ) {
                                    Text(
                                        R.string.book_appointment.string.uppercase(),
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                else{
                    LazyColumn(contentPadding = PaddingValues(24.dp)) {
                        items(bookings) { p ->
                            BookingItemUI(p)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("fdfdffd",e.message?:"")
        }
    }

    @Composable
    private fun BookingItemUI(p: AppointmentData) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 12.dp)
            .clickable {
                onBookingClicked(p)
            }
        ){
            Row(modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            colorResource(id = R.color.very_light_red),
                            shape = CircleShape
                        )) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = R.drawable.ic_medical_prescription),
                        tint = Color.Red,
                        contentDescription = null // decorative element
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(p.doctorName?:"Doctor",
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        p.category?:"Speciality",
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween){
                val textStyle = TextStyle(
                    color = Color.Gray, fontSize = 12.sp
                )
                val textStyle1 = TextStyle(
                    color = Color.Black, fontSize = 14.sp
                )
                Column {
                    Text(
                        "DATE",
                        style = textStyle
                    )
                    Text(p.date, style = textStyle1)
                }
                Column {
                    Text(
                        "TIME",
                        style = textStyle
                    )
                    Text(p.time,style = textStyle1)
                }
                Column {
                    Text(
                        "PATIENT NAME",
                        style = textStyle
                    )
                    Text(p.patientName?:"Patient", style = textStyle1)
                }
            }
        }
        Divider(
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
        )
    }

    private fun onBookingClicked(appointmentData: AppointmentData) {
        callback?.goToPage?.invoke(HomeActivity.PAGE.PRESCRIPTION,Bundle().apply {
            putSerializable(Constants.APPOINTMENT,appointmentData)
        })
    }

    private fun addAppointment() {
        val bottomSheetDialog = RoundBottomSheet(requireContext()){

        }
        bottomSheetDialog.setContentView(R.layout.choose_speciality_dialog_layout)

        val llv_general_practitioner = bottomSheetDialog.findViewById<View>(R.id.llv_general_practitioner)
        val llv_pediatrics = bottomSheetDialog.findViewById<View>(R.id.llv_pediatrics)

        val iv_general_practitioner = bottomSheetDialog.findViewById<View>(R.id.iv_general_practitioner)
        val iv_Pediatrics = bottomSheetDialog.findViewById<View>(R.id.iv_Pediatrics)

        llv_general_practitioner?.setOnClickListener {
            bottomSheetDialog.dismiss()
            onSpecialitySelected(AppointmentDoctorSpeciality.GENERAL)
        }
        llv_pediatrics?.setOnClickListener {
            bottomSheetDialog.dismiss()
            onSpecialitySelected(AppointmentDoctorSpeciality.PEDIATRICS)
        }
        /*****************************************/
        iv_general_practitioner?.setOnClickListener {
            bottomSheetDialog.dismiss()
            onSpecialitySelected(AppointmentDoctorSpeciality.GENERAL)
        }
        iv_Pediatrics?.setOnClickListener {
            bottomSheetDialog.dismiss()
            onSpecialitySelected(AppointmentDoctorSpeciality.PEDIATRICS)
        }
        /*****************************************/

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }
    var prices: AppointmentPrices? = null
    private fun onSpecialitySelected(speciality: AppointmentDoctorSpeciality) {
        repository.clearAppointmentData()
        repository.setAppointmentSpeciality(speciality.name)
        val bottomSheetDialog = RoundBottomSheet(requireContext()){

        }

        val binding = AppointmentTypeDialogBinding.inflate(layoutInflater)
        val root = binding.root
        bottomSheetDialog.setContentView(root)

        lifecycleScope.launch {
            processApi {
                repository.appointmentPrices().resp
            }
                .apply {
                    when(status){
                        ApiDispositionStatus.RESPONSE ->{
                            response?.apply {
                                if(success){
                                    prices = appointmentPrices
                                    prices?.let {
                                        binding.atvAppointmentTypes.setPrices(it)
                                        binding.pbLoading.visibility = View.GONE
                                        binding.btContinue.isEnabled = true
                                        binding.atvAppointmentTypes.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                        else->{
                            prices = null
                            R.string.something_went_wrong.string.toast(requireContext())
                        }
                    }
                }
        }



        binding.btContinue.setOnClickListener {
            val type = binding.atvAppointmentTypes.type
            if(type!= AppointmentTypeView.APPOINTMENT_TYPE.NONE){
                bottomSheetDialog.dismiss()
                onContinueAppointType(type,speciality)
            }
            else{
                R.string.please_choose_appoint_type.string.toast(requireContext())
            }
        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    private var dateSlotsData: List<DateSlot> = listOf()/*getDateSlots()*/
    private fun onContinueAppointType(
        type: AppointmentTypeView.APPOINTMENT_TYPE,
        speciality: AppointmentDoctorSpeciality
    ) {
        repository.setAppointmentType(type)
        var price = when(type){
            AppointmentTypeView.APPOINTMENT_TYPE.VIDEO -> prices?.videoAppointmentPrice?:0f
            AppointmentTypeView.APPOINTMENT_TYPE.CHAT -> prices?.chatAppointmentPrice?:0f
            AppointmentTypeView.APPOINTMENT_TYPE.VOICE -> prices?.callAppointmentPrice?:0f
            AppointmentTypeView.APPOINTMENT_TYPE.NONE -> 0f
        }
        repository.setAppointmentPrice(price)
        lifecycleScope.launch {
            (requireActivity() as? HomeActivity)?.wait = true
            processApi {
                repository.getSlots(type,speciality).resp
            }.apply {
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                dateSlotsData = this.dateSlots
                                openDateTime(type,speciality)
                            }
                            else{
                                message.toast(requireContext())
                            }
                        }
                    }
                    else->{
                        R.string.something_went_wrong.string.toast(requireContext())
                    }
                }
            }
            (requireActivity() as? HomeActivity)?.wait = false
        }

    }

    private fun getDateSlotsSummary(): List<DateSlotsSummary> {
        return dateSlotsData.mapIndexed() { index, it->
            DateSlotsSummary(
                id = index,
                slots = it.dayPartSlots.sumOf {
                    it.slots.size
                },
                date = it.date
            )
        }
    }

    var selectedSlotData = SelectedDateSlot()
    private fun openDateTime(
        type: AppointmentTypeView.APPOINTMENT_TYPE,
        speciality: AppointmentDoctorSpeciality
    ) {
        val bottomSheetDialog = RoundBottomSheet(requireContext()){

        }
        bottomSheetDialog.setContentView(R.layout.choose_date_time_dialog)

        val alvDates = bottomSheetDialog.findViewById<DatesView>(R.id.alv_dates)
        val tvDate = bottomSheetDialog.findViewById<TextView>(R.id.tv_date)
        val tvSlots = bottomSheetDialog.findViewById<TextView>(R.id.tv_slot)
        val svSlots = bottomSheetDialog.findViewById<SlotsView>(R.id.sv_slots)
        val tvNextAvailable = bottomSheetDialog.findViewById<TextView>(R.id.tv_next_available)
        val btContinue = bottomSheetDialog.findViewById<TextView>(R.id.bt_continue)

        btContinue?.setOnClickListener {
            if(
                selectedSlotData.date.isNotEmpty
                &&selectedSlotData.section.isNotEmpty
                &&selectedSlotData.time.isNotEmpty
            ){
                repository.setSelectedSlot(selectedSlotData, SelectedDateSlot::class.java)
                bottomSheetDialog.dismiss()
                callback?.goToPage?.invoke(HomeActivity.PAGE.ADD_PATIENT_DETAILS,null)
            }
            else{
                R.string.slot_not_selected.string.toast(requireContext())
            }
        }
        var data = getDateSlotsSummary()

        var nextAvailable = "WED, 24"

        val d = data[0]
        Log.d("slots_data",dateSlotsData.toString())
        selectedSlotData.date = dateSlotsData.getOrNull(0)?.date?:""
        svSlots?.setData(dateSlotsData[0].dayPartSlots)
        tvDate?.text = d.date.dateFormat(R.string.mysql_date_format.string,R.string.eee_mmm_d.string,true)
        tvSlots?.text = if(d.slots>0){R.string.no_slots.string}else{"${d.slots} ${R.string.slots.string} ${R.string.available.string}"}
        tvNextAvailable?.text = "${R.string.next_availability_on.string} $nextAvailable"
        alvDates?.onSelectedCallback = {
            selectedSlotData.date = dateSlotsData[it].date
            val dd = data[it]
            tvDate?.text = dd.date.dateFormat(R.string.mysql_date_format.string,R.string.eee_mmm_d.string,true)
            tvSlots?.text = if(dd.slots<=0){R.string.no_slots.string}else{"${dd.slots} ${R.string.slots.string} ${R.string.available.string}"}
            if(dd.slots<=0){
                tvSlots?.visibility = View.VISIBLE
                tvNextAvailable?.visibility = View.VISIBLE
                svSlots?.visibility = View.GONE
            }
            else{
                tvSlots?.visibility = View.GONE
                svSlots?.visibility = View.VISIBLE
                tvNextAvailable?.visibility = View.GONE
                svSlots?.setData(dateSlotsData[it].dayPartSlots)
            }
        }
        svSlots?.onChangeCallback = {pos,slotPos->
            val dateData = dateSlotsData.find {
                it.date==selectedSlotData.date
            }
            dateData?.apply {
                val dayPart = this.dayPartSlots.getOrNull(pos)
                selectedSlotData.section = dayPart?.id?:""
                dayPart?.apply {
                    selectedSlotData.time = this.slots.getOrNull(slotPos)?.timestamp?:""
                }
            }
        }
        alvDates?.setData(data)

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(callback: HomeActivity.ChildCallback) = MyBookingsFragment().apply {
            this.callback = callback
        }
    }
}