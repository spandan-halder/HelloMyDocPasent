package com.hellomydoc.data.appointment_booking_doctor_response

data class Doctor(
    val id: String,
    val experience: Int,
    val image: String,
    val name: String,
    val speciality: String,
    var selected: Boolean = false
)