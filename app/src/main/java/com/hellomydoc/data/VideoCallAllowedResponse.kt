package com.hellomydoc.data

data class VideoCallAllowedResponse(
    val success: Boolean,
    val message: String,
    val startTime: Long,
    val timeSpan: Long
)