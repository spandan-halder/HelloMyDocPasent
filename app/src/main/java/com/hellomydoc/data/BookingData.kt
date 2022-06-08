package com.hellomydoc.data

import com.hellomydoc.Dt

data class BookingData(
    val category: String,
    val doctorName: String,
    val id: String,
    val patientName: String,
    val timestamp: String
){
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