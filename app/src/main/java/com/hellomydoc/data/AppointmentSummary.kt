package com.hellomydoc.data

data class AppointmentSummary(
    val userId: String,
    val type: String,
    val speciality: String,
    val price: Float,
    val slot: SelectedDateSlot?,
    val patientId: String = "",
    val patientName: String,
    val patientAge: Int,
    val gender: String,
    val symptoms: String,
    val doctor: String,
    val paymentMethod: String,
)
