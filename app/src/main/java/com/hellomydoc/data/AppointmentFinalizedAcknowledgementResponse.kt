package com.hellomydoc.data

data class AppointmentFinalizedAcknowledgement(
    val finalized: Boolean,
    val message: String,
    val summary: String
)

data class AppointmentFinalizedAcknowledgementResponse(
    val success: Boolean,
    val message: String,
    val acknowledgement: AppointmentFinalizedAcknowledgement
)
