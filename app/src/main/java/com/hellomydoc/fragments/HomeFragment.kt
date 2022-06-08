package com.hellomydoc.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hellomydoc.*
import com.hellomydoc.R
import com.hellomydoc.activities.HomeActivity
import com.hellomydoc.data.*
import com.hellomydoc.data.slots.DateSlot
import com.hellomydoc.databinding.AppointmentTypeDialogBinding
import com.hellomydoc.databinding.FragmentHomeBinding
import com.hellomydoc.dialogs.CustomDialog
import com.hellomydoc.dialogs.RoundBottomSheet
import com.hellomydoc.views.*
import com.vxplore.audiovideocall.agoraaudiocall.CallBox
import com.vxplore.audiovideocall.videocall.VideoBox
import com.vxplore.audiovideocall.videocall.models.AllowedResponse
import com.vxplore.audiovideocall.videocall.models.Ids
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val dateTimeTrigger = mutableStateOf(0L)
    private var callback: HomeActivity.ChildCallback? = null
    private var dateSlotsData: List<DateSlot> = listOf()/*getDateSlots()*/


    var prices: AppointmentPrices? = null

    private var upcomingAppointmentsLoading: Boolean = true
    private var recentBookingsLoading: Boolean = true
    private var _upcomingAppointments: List<AppointmentData>? = emptyList()
    private var upcomingAppointments: List<AppointmentData>?
        get(){return _upcomingAppointments}
        set(value){
            _upcomingAppointments = value
            setupUpcomingAppointments()
        }

    private var _recentBookings: List<AppointmentData>? = emptyList()

    private var recentBookings: List<AppointmentData>?
        get(){return _recentBookings}
        set(value){
            _recentBookings = value
            setupRecentBookings()
        }

    private fun setupUpcomingAppointments() {
        binding.glvUpcomingAppointments.configure(
            AnyListView.Configurator(
                isSlider = false,
                animationDuration = 100,
                animation = {position->
                    R.anim.fall_down_animation
                },
                overScroll = false,
                state = if(upcomingAppointmentsLoading) AnyListView.STATE.LOADING else AnyListView.STATE.DATA,
                itemType = {position->position},
                itemCount = { getAppointmentItemCount()},
                itemView = {
                    AppointmentView(requireContext()).apply {
                        layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT)
                        data = getAppointmentItem(it)
                    }
                },
                isExpanded = {true},
                onView = {pos,view->
                    val d = getAppointmentItem(pos)
                    (view as? AppointmentView)?.data = d
                    (view as? AppointmentView)?.setCallback(
                        wholeViewClickListener = {
                            onAppointmentClicked(d?:return@setCallback)
                        },
                        typeViewClickListener = {
                            when(d?.type?.uppercase()){
                                AppointmentTypeView.APPOINTMENT_TYPE.CHAT.name->openChat(d)
                                AppointmentTypeView.APPOINTMENT_TYPE.VIDEO.name->openVideo(d)
                                AppointmentTypeView.APPOINTMENT_TYPE.VOICE.name->goToVoiceCall(d)
                            }
                        }
                    )
                }
            ))
    }

    private fun onAppointmentClicked(appointmentData: AppointmentData) {
        callback?.goToPage?.invoke(HomeActivity.PAGE.PRESCRIPTION,Bundle().apply {
            putSerializable(Constants.APPOINTMENT,appointmentData)
        })
    }

    private fun goToVoiceCall(it: AppointmentData) {
        CallBox.start(
            it.patientId?:return,
            it.doctorId?:return,
            it.id?:return,
            requireActivity(),
            System.currentTimeMillis(),
            15*60*1000
        )
    }

    private fun openVideo(d: AppointmentData) {
        VideoBox.callback = object: VideoBox.Callback{
            override val ids: Ids?
                get() = Ids(
                    d.patientId!!,
                    d.doctorId!!,
                    d.id!!,
                    d.patientId,
                    d.doctorName!!
                )
            override val appContext: Application
                get() = requireActivity().application

            override fun onApproving() {

            }

            override suspend fun checkAllowed(channelId: String, userId: String): AllowedResponse? {
                /*val startTime = System.currentTimeMillis()
                return AllowedResponse(true,"allowed",startTime,15*60*1000)*/

                /*val r = repository.checkVideoCallAllowed(channelId,userId).resp
                if(r.body?.success==true){
                    val startTime = (r.body?.startTime?:return null).utcToCurrentTimeZoneMillis
                    val timeSpan = r.body?.timeSpan?:return null
                    val endTime = startTime + timeSpan
                    val now = System.currentTimeMillis()
                    val left = endTime - now
                    return when {
                        left<=0 -> {
                            try {
                                Toast.makeText(requireActivity(), "Time crossed", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                            }
                            null
                        }
                        startTime>now -> {
                            try {
                                Toast.makeText(requireActivity(), "Can not proceed before schedule", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                            }
                            null
                        }
                        else -> {
                            AllowedResponse(true,"allowed",startTime,timeSpan)
                        }
                    }
                }
                else{
                    return null
                }*/
                val startTime = System.currentTimeMillis()
                return AllowedResponse(true,"allowed",startTime,15*60*1000)
            }
        }
        VideoBox.start(requireActivity(),d.id?:return,d.patientId?:return)
        /*callback?.goToPage?.invoke(HomeActivity.PAGE.VIDEO,Bundle().apply {
            val doctorId = d.doctorId?:""
            val patientId = d.patientId?:""
            val appointmentId = d.id?:""
            if(doctorId.isNotEmpty&&patientId.isNotEmpty&&appointmentId.isNotEmpty){
                putString("doctor",doctorId)
                putString("patent",patientId)
                putString("appointmentId",appointmentId)
            }
        })*/
    }

    private fun openChat(d: AppointmentData) {
        callback?.goToPage?.invoke(HomeActivity.PAGE.CHAT,Bundle().apply {
            val doctorId = d.doctorId?:""
            val myId = d.patientId?:""
            if(doctorId.isEmpty){
                try {
                    Toast.makeText(
                        requireContext(),
                        "Doctor ID not found",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                }
            }
            else{
                putString(Constants.PEER_ID_KEY,"doctor_$doctorId")
                putString(Constants.MY_ID,"client_$myId")
            }
        })
    }

    private lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            setupProfileImageAction()
            fetchUser()
            fetchUpcomingAppointments()
            fetchRecentBookings()
        }

        binding.tvUserName.startShimmer()

        binding.llvGeneralPractitioner.setOnClickListener {
            onGeneralPractitionerClick()
        }
        binding.ivGeneralPractitioner.setOnClickListener {
            onGeneralPractitionerClick()
        }

        binding.llvPediatrics.setOnClickListener {
            onPediatricsClick()
        }
        binding.ivPediatrics.setOnClickListener {
            onPediatricsClick()
        }

        binding.tvViewAllUpcomingAppointment.setOnClickListener {
            callback?.goToPage?.invoke(HomeActivity.PAGE.CONSULTATIONS,null)
        }

        binding.tvViewAllRecentBookings.setOnClickListener {
            callback?.goToPage?.invoke(HomeActivity.PAGE.BOOKINGS,null)
        }

        setupUpcomingAppointments()
        setupRecentBookings()
    }

    private fun setupProfileImageAction() {
        binding.profileImage.setOnClickListener {
            openProfilePage()
        }
    }

    private fun openProfilePage() {
        callback?.goToPage?.invoke(HomeActivity.PAGE.PROFILE,null)
    }

    override fun onResume() {
        super.onResume()
        checkIfAnyLastAppointmentFinalized()
    }

    private fun checkIfAnyLastAppointmentFinalized() {
        val lastAppointmentFinalizedId = repository.getLastAppointmentFinalizedId()
        if(lastAppointmentFinalizedId.isNotEmpty){
            lifecycleScope.launch {
                lifecycleScope.launch {
                    processApi {
                        repository.appointmentFinalizedAcknowledgement(lastAppointmentFinalizedId).resp
                    }
                        .apply {
                            when(status){
                                ApiDispositionStatus.RESPONSE ->{
                                    response?.apply {
                                        if(success){
                                            CustomDialog
                                                .create(requireContext())
                                                .layout(R.layout.appointment_booked_thank_you)
                                                .noBackground()
                                                .size(screenWidth - 48.dpToPx)
                                                .with{
                                                    val tv_message = findViewById<TextView>(R.id.tv_message)
                                                    val tv_summary = findViewById<TextView>(R.id.tv_summary)
                                                    val bt_go = findViewById<TextView>(R.id.bt_go)

                                                    tv_message.text = acknowledgement.message
                                                    tv_summary.text = acknowledgement.summary
                                                    setOnDismissListener {
                                                        repository.setLastAppointmentFinalizedId("")
                                                    }
                                                    bt_go.setOnClickListener {
                                                        dismiss()
                                                        goToConsultations()
                                                    }
                                                }
                                                .show()
                                        }
                                    }
                                }
                                else->{
                                    prices = null
                                    //R.string.something_went_wrong.string.toast(requireContext())
                                }
                            }
                        }
                }
            }
        }
    }

    private fun goToConsultations() {
        callback?.goToPage?.invoke(HomeActivity.PAGE.CONSULTATIONS,null)
    }

    private fun onPediatricsClick() {
        onSpecialitySelected(AppointmentDoctorSpeciality.PEDIATRICS)
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
                repository.setSelectedSlot(selectedSlotData,SelectedDateSlot::class.java)
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
            Log.d("fkljfsdsdlfjlds","$pos.$slotPos")
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

    private fun showAvailableSlots(it: Int) {

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
            if(type!=AppointmentTypeView.APPOINTMENT_TYPE.NONE){
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

    private fun onGeneralPractitionerClick() {
        onSpecialitySelected(AppointmentDoctorSpeciality.GENERAL)
    }

    private suspend fun fetchUser() {
        processApi {
            repository.getUser().resp
        }.apply {
            when(status){
                ApiDispositionStatus.RESPONSE ->{
                    response?.apply {
                        if(success){
                            binding.tvUserName.text = user.name
                            binding.tvUserName.cancelShimmer()
                            binding.profileImage.setImage(user.image)
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchRecentBookings() {
        processApi {
            repository.fetchRecentBookings().resp
        }.apply {
            recentBookingsLoading = false
            when(status){
                ApiDispositionStatus.RESPONSE ->{
                    response?.apply {
                        if(success){
                            recentBookings = appointments
                        }
                    }
                }
                else->{
                    recentBookings = emptyList()
                }
            }
        }
    }

    private suspend fun fetchUpcomingAppointments() {
        processApi {
            repository.fetchFewUpcomingAppointments().resp
        }
            .apply {
                upcomingAppointmentsLoading = false
                when(status){
                    ApiDispositionStatus.RESPONSE ->{
                        response?.apply {
                            if(success){
                                upcomingAppointments = appointments
                            }
                        }
                    }
                    else->{
                        upcomingAppointments = emptyList()
                    }
                }
            }
    }

    private fun setupRecentBookings() {
        binding.glvRecentBookings.configure(
            AnyListView.Configurator(
                animationDuration = 100,
                animation = {position->
                    R.anim.fall_down_animation
                },
                overScroll = false,
                state = if(recentBookingsLoading) AnyListView.STATE.LOADING else AnyListView.STATE.DATA,
                itemType = {position->position},
                itemCount = { _recentBookings?.size?:0},
                itemView = {
                    BookingView(requireContext()).apply {
                        layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT)
                        data = _recentBookings?.getOrNull(it)
                        setOnClickListener {
                            data?.let { onBookingClicked(it) }
                        }
                    }
                },
                isExpanded = {true}
            ))
    }

    private fun onBookingClicked(appointmentData: AppointmentData) {
        callback?.goToPage?.invoke(HomeActivity.PAGE.PRESCRIPTION,Bundle().apply {
            putSerializable(Constants.APPOINTMENT,appointmentData)
        })
    }

    private fun getAppointmentItemCount():Int {
        return _upcomingAppointments?.size?:0
    }

    private fun getAppointmentItem(it: Int): AppointmentData? {
        return _upcomingAppointments?.getOrNull(it)
    }

    override fun onDestroyView() {
        callback = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(callback: HomeActivity.ChildCallback) = HomeFragment().apply {
            this.callback = callback
        }
    }
}