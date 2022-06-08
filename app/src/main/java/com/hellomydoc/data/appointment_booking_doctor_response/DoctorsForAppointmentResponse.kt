package com.hellomydoc.data.appointment_booking_doctor_response

data class DoctorsForAppointmentResponse(
    val doctors: List<Doctor>,
    val message: String,
    val success: Boolean
)