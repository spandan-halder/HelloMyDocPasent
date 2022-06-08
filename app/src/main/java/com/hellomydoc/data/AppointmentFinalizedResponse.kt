package com.hellomydoc.data

data class AppointmentFinalizedResponse(
    val success: Boolean,
    val message: String,
    val appointmentId: String,
    val waitForPaymentDone: Boolean
)