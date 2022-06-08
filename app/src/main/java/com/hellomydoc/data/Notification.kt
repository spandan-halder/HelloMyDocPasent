package com.hellomydoc.data

data class Notification(
    val `data`: String,
    val hash_id: String,
    val id: String,
    val image: String,
    val message: String,
    val need_acknowledgement: String,
    val status: String,
    val subtitle: String,
    val timestamp: String,
    val title: String,
    val update_timestamp: String,
    val user_id: String
)