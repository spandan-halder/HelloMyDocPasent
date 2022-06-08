package com.hellomydoc.data

data class MedicalRecordsResponse(
    val success: Boolean,
    val message: String,
    val records: List<MedicalRecord>
)
