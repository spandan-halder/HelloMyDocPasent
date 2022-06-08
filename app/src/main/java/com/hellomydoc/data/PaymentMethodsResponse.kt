package com.hellomydoc.data

data class PaymentMethodsResponse(
    val message: String,
    val paymentMethods: List<PaymentMethod>,
    val success: Boolean
)