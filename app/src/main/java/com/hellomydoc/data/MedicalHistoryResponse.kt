package com.hellomydoc.data

data class MedicalHistoryResponse(
    val success: Boolean,
    val message: String,
    val history: List<HistoryItem>
)

data class HistoryItem(
    val type: String,
    val prescription: PrescriptionShort?,
    val record: MedicalRecordShort?
)

data class MedicalRecordShort(
    val id: String,
    val type: String,
    val attachments: String,
    val timestamp: Long,
)
