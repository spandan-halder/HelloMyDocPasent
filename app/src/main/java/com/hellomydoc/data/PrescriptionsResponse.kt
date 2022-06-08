package com.hellomydoc.data

import com.google.gson.annotations.SerializedName

data class PrescriptionsResponse(
    val success       : Boolean?                 = null,
    val message       : String?                  = null,
    val prescriptions : ArrayList<Prescription> = arrayListOf()
)

data class Prescription(
    val patientSummary  : String?,
    val patientSymptoms : String?,
    val appointmentType : String?,
    val timestamp       : Long?,
    val prescriptionId  : String?,
    val appointmentId   : String?
)
