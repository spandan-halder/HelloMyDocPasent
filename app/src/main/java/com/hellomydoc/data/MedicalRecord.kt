package com.hellomydoc.data

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.io.Serializable

data class MedicalRecord(
    val id: String?,
    val name: String?,
    val timestamp: String?,
    val type: String?,
    val attachments: List<String>?,
    val user_id: String?,
    val patient: String?,
    val patientName: String?
): Serializable{
    val date: String
        get(){
            return if(timestamp!=null){
                try {
                    val vars = timestamp.split("[-: ]".toRegex()).map {
                        it.toInt()
                    }
                    val df = DateTimeFormat.forPattern("EEE, MMM d, yyyy")
                    val dt = DateTime(
                        vars[0],
                        vars[1],
                        vars[2],
                        vars[3],
                        vars[4],
                        vars[5])
                    df.print(dt)
                } catch (e: Exception) {
                    return ""
                }
            } else{
                ""
            }
        }
}
