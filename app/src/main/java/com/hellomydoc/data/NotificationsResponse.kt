package com.hellomydoc.data

data class NotificationsResponse(
    val message: String,
    val notifications: List<Notification>?,
    val success: Boolean
)