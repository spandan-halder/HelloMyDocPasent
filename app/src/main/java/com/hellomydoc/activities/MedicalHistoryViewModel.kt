package com.hellomydoc.activities

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellomydoc.User
import com.hellomydoc.check
import com.hellomydoc.data.HistoryItem
import com.hellomydoc.repository
import com.hellomydoc.resp
import kotlinx.coroutines.launch

class MedicalHistoryViewModel : ViewModel() {

    private val _transactionMessage = mutableStateOf("")
    val transactionMessage: State<String> = _transactionMessage

    private val _leave = mutableStateOf(false)
    val leave: State<Boolean> = _leave
    
    private val _history = mutableStateListOf<HistoryItem>()
    val history: SnapshotStateList<HistoryItem> = _history
    
    private val _historyState = mutableStateOf(DataState.LOADING)
    val historyState: State<DataState> = _historyState
    ////////////////////////
    fun onBack() {
        leave()
    }

    private fun leave(){
        _leave.check()
    }

    fun addHistory() {

    }

    ///////////////////////////
    init {
        fetchMedicalHistory()
    }

    fun fetchMedicalHistory() {
        viewModelScope.launch {
            _historyState.value = DataState.LOADING
            val response = try {
                repository.fetchMedicalHistory().resp
            } catch (e: Exception) {
                null
            }
            _historyState.value = DataState.CHECKING
            if(response?.isSuccess==true){
                _historyState.value = DataState.IO_SUCCESS
                val data = response.body
                if(data?.success==true){
                    _historyState.value = DataState.TRANSACTION_SUCCESS
                    _history.clear()
                    _history.addAll(data.history)
                    _historyState.value = DataState.DATA
                }
                else{
                    _historyState.value = DataState.TRANSACTION_FAILED
                    _transactionMessage.value = data?.message?:""
                }
            }
            else{
                _historyState.value = DataState.IO_FAILED
            }
        }
    }
}

