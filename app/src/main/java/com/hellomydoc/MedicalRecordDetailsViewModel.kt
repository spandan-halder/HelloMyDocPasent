package com.hellomydoc

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.hellomydoc.data.MedicalRecord

class MedicalRecordDetailsViewModel: ViewModel() {
    var record = mutableStateOf<MedicalRecord?>(null)
}