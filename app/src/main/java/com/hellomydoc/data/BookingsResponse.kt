package com.hellomydoc.data

data class BookingsResponse(
    val bookings: List<Booking>,
    val message: String,
    val success: Boolean
)