package com.hellomydoc.data.slots

data class Slot(
    val id: String,
    val timestamp: String,
    var selected:Boolean = false
)