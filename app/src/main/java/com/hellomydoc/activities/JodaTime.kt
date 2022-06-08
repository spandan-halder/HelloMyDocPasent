package com.hellomydoc.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hellomydoc.R
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Years


class JodaTime : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joda_time)

        val today = DateTime()
        val dob = DateTime("1987-05-29")
        val duration = Duration(today,dob)
        val age = Years.yearsBetween(dob,today)
        Log.d("fddfdfdsfsffere","${age.years}")

        val ageOnDate = 34
        var dateOfAge = DateTime("1995-12-31")
        var ageToday = ageOnDate + Years.yearsBetween(dateOfAge,today).years
        Log.d("fddfdfdsfsffere","${ageToday}")
    }
}