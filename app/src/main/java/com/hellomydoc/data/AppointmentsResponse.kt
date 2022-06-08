package com.hellomydoc.data

data class AppointmentsResponse(
    val success: Boolean = false,
    val message: String = "",
    val appointments: List<AppointmentData> = emptyList()
)