package com.hellomydoc.data

data class RecentBookingsResponse(
    val success: Boolean = false,
    val message: String = "",
    val bookings: List<BookingData> = emptyList()
)
