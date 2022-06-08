package com.hellomydoc.data

import com.hellomydoc.data.slots.Slot

data class SlotSection(
    val id: String,
    val name: String,
    val slots: List<Slot>?
)