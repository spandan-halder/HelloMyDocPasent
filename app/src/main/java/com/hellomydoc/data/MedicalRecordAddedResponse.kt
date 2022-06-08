package com.hellomydoc.data

data class MedicalRecordAddedResponse(
    val success: Boolean,
    val message: String,
    val recordAdded: Boolean
)