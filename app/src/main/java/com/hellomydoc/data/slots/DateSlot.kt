package com.hellomydoc.data.slots

data class DateSlot(
    val dayPartSlots: List<DayPartSlot>,
    val date: String,
    val id: String
)