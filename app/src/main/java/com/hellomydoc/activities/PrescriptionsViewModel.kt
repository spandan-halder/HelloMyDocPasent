package com.hellomydoc.activities

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellomydoc.check
import com.hellomydoc.data.Prescription
import com.hellomydoc.data.PrescriptionDetails
import com.hellomydoc.repository
import com.hellomydoc.resp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DataState{
    LOADING,
    IO_FAILED,
    CHECKING,
    IO_SUCCESS,
    TRANSACTION_SUCCESS,
    TRANSACTION_FAILED,
    DATA,
    NO_DATA
}

enum class PrescriptionPages{
    LIST,
    DETAILS
}

class PrescriptionsViewModel: ViewModel() {
    private val _currentPrescription = mutableStateOf<Prescription?>(null)
    val currentPrescription: State<Prescription?> = _currentPrescription

    private val _prescriptionDetails = mutableStateOf<PrescriptionDetails?>(null)
    val prescriptionDetails: State<PrescriptionDetails?> = _prescriptionDetails

    private val _currentPage = mutableStateOf(PrescriptionPages.LIST)
    val currentPage: State<PrescriptionPages> = _currentPage

    private val _leave = mutableStateOf(false)
    val leave: State<Boolean> = _leave

    private val _listState = mutableStateOf(DataState.LOADING)
    val listState: State<DataState> = _listState

    private val _transactionMessage = mutableStateOf("")
    val transactionMessage: State<String> = _transactionMessage

    private val _prescriptions = mutableStateListOf<Prescription>()
    val prescriptions: SnapshotStateList<Prescription> = _prescriptions
    //////////////////////////////////////////////////
    private val _detailsState = mutableStateOf(DataState.LOADING)
    val detailsState: State<DataState> = _detailsState
    ////////////////////////
    fun onBack() {
        leave()
    }

    private fun leave(){
        _leave.check()
    }

    fun onPrescriptionDetailsClick(prescription: Prescription) {
        _currentPrescription.value = prescription
        _currentPage.value = PrescriptionPages.DETAILS
        _detailsState.value = DataState.LOADING
        fetchPrescriptionDetails(prescription.prescriptionId?:return)
    }

    fun fetchPrescriptionDetails(prescriptionId: String) {
        viewModelScope.launch {
            _detailsState.value = DataState.LOADING
            val response = try {
                repository.fetchPrescriptionDetails(prescriptionId).resp
            } catch (e: Exception) {
                null
            }
            _detailsState.value = DataState.CHECKING
            if(response?.isSuccess==true){
                _detailsState.value = DataState.IO_SUCCESS
                val data = response.body
                if(data?.success==true){
                    _detailsState.value = DataState.TRANSACTION_SUCCESS
                    _prescriptionDetails.value = data.prescriptionDetails
                    _detailsState.value = DataState.DATA
                }
                else{
                    _detailsState.value = DataState.TRANSACTION_FAILED
                    _transactionMessage.value = data?.message?:""
                }
            }
            else{
                _detailsState.value = DataState.IO_FAILED
            }
        }
    }

    init {
        fetchList()
    }

    fun fetchList() {
        viewModelScope.launch {
            _listState.value = DataState.LOADING
            val response = try {
                repository.fetchPrescriptions().resp
            } catch (e: Exception) {
                null
            }
            _listState.value = DataState.CHECKING
            if(response?.isSuccess==true){
                _listState.value = DataState.IO_SUCCESS
                val data = response.body
                if(data?.success==true){
                    _listState.value = DataState.TRANSACTION_SUCCESS
                    _prescriptions.clear()
                    if(data.prescriptions.isEmpty()){
                        _listState.value = DataState.NO_DATA
                    }
                    else{
                        _listState.value = DataState.DATA
                        _prescriptions.addAll(data.prescriptions)
                    }
                }
                else{
                    _listState.value = DataState.TRANSACTION_FAILED
                    _transactionMessage.value = data?.message?:""
                }
            }
            else{
                _listState.value = DataState.IO_FAILED
            }
        }
    }

    fun leaveDetails() {
        _currentPage.value = PrescriptionPages.LIST
        _currentPrescription.value = null
        _prescriptionDetails.value = null
        _transactionMessage.value = ""
    }
}