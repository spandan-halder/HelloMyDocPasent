package com.hellomydoc.data.doctorDetails

data class DoctorDetailsResponse(
    val details: DoctorDetails,
    val message: String,
    val success: Boolean
)