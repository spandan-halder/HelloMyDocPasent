package com.hellomydoc.data

data class PaymentMethod(
    val id: String,
    val icon: String,
    val name: String,
    var selected: Boolean = false
)