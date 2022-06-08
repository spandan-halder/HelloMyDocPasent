package com.hellomydoc.data

data class OTPResponse(
    val success: Boolean,
    val message: String,
    val otp: String,
)