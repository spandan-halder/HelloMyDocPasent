package com.hellomydoc.data.doctorDetails

data class DoctorDetails(
    val availabilities: List<Availability>,
    val id: String,
    val name: String,
    val profile_pic: String,
    val specialities: List<Speciality>
){
    val specialitiesText: String
    get(){
        return specialities.joinToString(", ") {
            it.name.uppercase()
        }
    }
}