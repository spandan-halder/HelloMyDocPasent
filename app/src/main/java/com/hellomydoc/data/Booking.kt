package com.hellomydoc.data

import com.hellomydoc.Dt

data class Booking(
    val date_time: String?,
    val doctorName: String?,
    val hash_id: String?,
    val patientName: String?,
    val speciality: String?,
    val timestamp: String?
)
{
    val date: String
        get(){
            return if(timestamp!=null){
                Dt.millis_to_date(timestamp.toLong(),"EEE, MMM d, yyyy")
            } else{
                ""
            }
        }
    val time: String
        get(){
            return if(timestamp!=null){
                Dt.millis_to_date(timestamp.toLong(),"K:mm a")
            } else{
                ""
            }
        }
}