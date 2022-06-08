package com.hellomydoc.data.slots

data class DayPartSlot(
    val id: String,
    val name: String,
    val slots: List<Slot>
)