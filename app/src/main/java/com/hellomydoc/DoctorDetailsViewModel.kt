package com.hellomydoc

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hellomydoc.data.doctorDetails.DoctorDetails
import kotlinx.coroutines.launch

class DoctorDetailsViewModel: ViewModel() {
    private val _doctorDetails = mutableStateOf<DoctorDetails?>(null)
    val doctorDetails:State<DoctorDetails?> = _doctorDetails

    private val _pageState = mutableStateOf(PageState.INITIAL)
    val pageState:State<PageState> = _pageState
    private val _doctorId = mutableStateOf("")
    private val _doctorName = mutableStateOf("")
    val doctorName: State<String> = _doctorName
    fun setDoctor(doctorId: String, doctorName: String) {
        _doctorId.value = doctorId
        _doctorName.value = doctorName
        fetchDetails()
    }

    private fun fetchDoctorDetails() {
        viewModelScope.launch {
            _pageState.value = PageState.LOADING
            val r = repository.fetchDoctorDetails(_doctorId.value).resp
            if(!r.isSuccess){
                _pageState.value = PageState.FAILED
                return@launch
            }
            if(r.body==null){
                _pageState.value = PageState.ERROR
                return@launch
            }
            val resp = r.body
            if(resp==null){
                _pageState.value = PageState.ERROR
                return@launch
            }
            if(!resp.success){
                _pageState.value = PageState.FAILED
                return@launch
            }
            _doctorDetails.value = resp.details
            _pageState.value = PageState.HAS_DATA
        }
    }

    fun fetchDetails() {
        if(_doctorId.value.isNotEmpty){
            fetchDoctorDetails()
        }
    }
}