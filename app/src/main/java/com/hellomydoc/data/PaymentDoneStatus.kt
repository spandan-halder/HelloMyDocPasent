package com.hellomydoc.data

data class PaymentDoneStatus(
    val success: Boolean,
    val message: String,
    val paymentDone: Boolean
)
