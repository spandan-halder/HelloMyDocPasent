package com.hellomydoc.data

import com.google.gson.annotations.SerializedName

data class PrescriptionDetailsResponse(
    val success: Boolean,
    val message: String,
    val prescriptionDetails: PrescriptionDetails
)

data class PrescriptionDetails(
    val id: String,
    val appointmentId: String,
    val medicines: List<PrescribedMedicine>,
    val labTests: List<String>
)

data class PrescriptionShort(
    val patientSummary  : String? = null,
    val patientSymptoms : String? = null,
    val appointmentType : String? = null,
    val timestamp       : Long?    = null,
    val prescriptionId  : String? = null,
    val appointmentId   : String? = null
)

data class PrescribedMedicine(
    val name: String,
    val doseValue: Float,
    val doseUnit: String,
    val timePeriodValue: Int,
    val timePeriodUnit: String,
    val frequency: String
)
