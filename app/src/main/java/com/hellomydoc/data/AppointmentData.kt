package com.hellomydoc.data

import com.hellomydoc.Dt
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.Serializable
import java.util.*


data class AppointmentData(
    val category: String?,
    val doctorName: String?,
    val doctorId: String?,
    val image: String?,
    val id: String?,
    val patientName: String?,
    val patientId: String?,
    val timestamp: String?,
    val type: String?,
    val prescriptionId: String?
): Serializable {
    val date: String
        get(){
            return if(timestamp!=null){
                //Dt.millis_to_date(timestamp.toLong(),"EEE, MMM d, yyyy")
                /*DateTime(timestamp.toLong(), DateTimeZone.UTC).toString(DateTimeFormat.forPattern("EEE, MMM d, yyyy").withLocale(
                    Locale.US))*/
                val df = DateTimeFormat.forPattern("EEE, MMM d, yyyy")
                val dt = DateTime(timestamp.toLong(), DateTimeZone.UTC)
                df.print(dt)
            } else{
                ""
            }
        }
    val time: String
        get(){
            return if(timestamp!=null){
                //Dt.millis_to_date(timestamp.toLong(),"K:mm a")
                //DateTime(timestamp, DateTimeZone.UTC).toString(DateTimeFormat.forPattern("K:mm a"))
                val df = DateTimeFormat.forPattern("HH:mm a")
                val dt = DateTime(timestamp.toLong(), DateTimeZone.UTC)
                df.print(dt)
            } else{
                ""
            }
        }

    companion object{
    }
}