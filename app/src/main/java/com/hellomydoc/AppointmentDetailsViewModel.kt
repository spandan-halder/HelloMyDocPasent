package com.hellomydoc

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellomydoc.data.AppointmentData
import com.hellomydoc.data.PrescriptionDetails
import kotlinx.coroutines.launch

class AppointmentDetailsViewModel : ViewModel() {
    private val _prescriptionLoadState = mutableStateOf(PageState.INITIAL)
    val prescriptionLoadState: State<PageState> = _prescriptionLoadState

    private val _prescription = mutableStateOf<PrescriptionDetails?>(null)
    val prescription: State<PrescriptionDetails?> = _prescription

    private var _appointmentData: AppointmentData? = null

    val hasPrescription: Boolean
    get(){
        return _appointmentData?.prescriptionId!=null
    }

    fun setAppointmentData(appointmentData: AppointmentData) {
        this._appointmentData = appointmentData
        fetchPrescription()
    }

    val appointmentType: String
    get(){
        return _appointmentData?.type?.uppercase()?:""
    }

    val appointmentDateTime: String
        get() {
            val date = _appointmentData?.date
            val time = _appointmentData?.time

            return "$date\n$time"
        }

    val doctorImage: String
    get(){
        return _appointmentData?.image?:""
    }

    val doctorName: String
    get(){
        return _appointmentData?.doctorName?:""
    }

    val doctorSpeciality: String
    get(){
        return _appointmentData?.category?:""
    }

    val patientName: String
    get(){
        return _appointmentData?.patientName?:""
    }

    val doctorId: String
    get(){
        return _appointmentData?.doctorId?:""
    }

    private fun fetchPrescriptionDetails() {
        if(_appointmentData?.prescriptionId==null){
            return
        }
        viewModelScope.launch {
            _prescriptionLoadState.value = PageState.LOADING
            val r = repository.fetchPrescriptionDetails(_appointmentData?.prescriptionId?:return@launch).resp
            if(!r.isSuccess){
                _prescriptionLoadState.value = PageState.FAILED
                return@launch
            }
            if(r.body==null){
                _prescriptionLoadState.value = PageState.ERROR
                return@launch
            }
            val response = r.body
            if(response?.success != true){
                _prescriptionLoadState.value = PageState.FAILED
                return@launch
            }
            _prescription.value = response.prescriptionDetails
            _prescriptionLoadState.value = PageState.HAS_DATA
        }
    }

    fun fetchPrescription() {
        fetchPrescriptionDetails()
    }
}