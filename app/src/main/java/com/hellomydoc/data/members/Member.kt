package com.hellomydoc.data.members

import org.joda.time.DateTime
import org.joda.time.Years

data class Member(
    val dob: String,
    val gender: String,
    val image: String,
    val name: String,
    val user_id: String,
    val age_on_date: Int,
    val date_of_age: String
) {
    val genderShort: String
    get(){
        return when(gender.uppercase()){
            "MALE"->"M"
            "FEMALE"->"F"
            else -> "OTHER"
        }
    }
    val age: Int
        get() {
            return if(age_on_date>0){
                val doa = DateTime(date_of_age)
                val t = DateTime()
                val e = Years.yearsBetween(doa,t).years
                e + age_on_date
            } else{
                try {
                    val d = DateTime(dob)
                    val t = DateTime()
                    Years.yearsBetween(d, t).years
                } catch (e: Exception) {
                    0
                }
            }
        }
}