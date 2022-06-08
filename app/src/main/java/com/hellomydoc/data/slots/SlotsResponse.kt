package com.hellomydoc.data.slots

data class SlotsResponse(
    val dateSlots: List<DateSlot>,
    val message: String,
    val success: Boolean
)