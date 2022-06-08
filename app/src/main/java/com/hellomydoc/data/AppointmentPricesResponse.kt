package com.hellomydoc.data

data class AppointmentPricesResponse(
    val appointmentPrices: AppointmentPrices,
    val message: String,
    val success: Boolean
)