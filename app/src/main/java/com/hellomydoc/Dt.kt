package com.hellomydoc

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Dt {
    //dd/MM/yyyy hh:mm:ss.SSS
    const val DATE_FORMAT_yyyyMMdd = "yyyyMMdd"
    const val DATE_FORMAT_HH_mm = "HH:mm"
    const val DATE_FORMAT_hh_mm_a = "hh:mm a"
    const val DATE_FORMAT_yyyy_MM_dd = "yyyy_MM_dd"
    const val DATE_DB_DATE = "yyyy-MM-dd"
    const val DATE_DB_DATE_TIME = "yyyy-MM-dd HH:mm:ss"
    const val DATE_DATE_SLASH = "MM/dd/yy"
    const val DATE_DATE_SLASH_SPACE_TIME = "MM/dd/yy HH:mm"
    const val DATE_DB_TIME_HH_mm_ss_sss = "HH:mm:ss.sss"
    const val DATE_DB_FULL_DATE = "MM_dd_yy_HH_mm_ss_sss"
    const val DATE_FORMAT_MM_dd_yyyy = "MM-dd-yyyy"
    fun millis_to_date(milliSeconds: Long, dateFormat: String?): String {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun today(dateFormat: String?): String {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        return formatter.format(calendar.time)
    }

    fun calendar(format: String?, input: String?): Calendar? {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        sdf.isLenient = false
        return try {
            cal.time = sdf.parse(input)
            cal
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun calendar(millis: Long): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal
    }

    fun calendarStart(format: String?, input: String?): Calendar? {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        return try {
            cal.time = sdf.parse(input)
            cal[Calendar.MILLISECOND] = 0
            cal[Calendar.SECOND] = 0
            cal[Calendar.MINUTE] = 0
            cal[Calendar.HOUR_OF_DAY] = 0
            cal
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun calendarEnd(format: String?, input: String?): Calendar? {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        return try {
            cal.time = sdf.parse(input)
            cal[Calendar.MILLISECOND] = 999
            cal[Calendar.SECOND] = 59
            cal[Calendar.MINUTE] = 59
            cal[Calendar.HOUR_OF_DAY] = 23
            cal
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun relativeDate(rel: Int, dateFormat: String?): String {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, rel)
        return formatter.format(calendar.time)
    }

    fun dateString(format: String?, calendar: Calendar?): String {
        val formatter = SimpleDateFormat(format)
        return formatter.format(calendar!!.time)
    }

    fun shortMonthString(from_month: Int): String {
        val dateString = String.format(Locale.ENGLISH, "01-%02d-1990", from_month)
        return dateString("MMM", calendar("dd-MM-yyyy", dateString))
    }

    fun shortWeekDay(day: Calendar?): String {
        return dateString("E", day)
    }

    fun mintLater(format: String?, time: String?, rel: Int): String {
        val c = calendar(format, time)
        if (c != null) {
            c.add(Calendar.MINUTE, rel)
            return dateString(format, c)
        }
        return ""
    }

    fun millis(format: String?, time: String?): Long {
        val c = calendar(format, time)
        return c?.timeInMillis ?: 0
    }

    fun format(inputFormat: String?, outputFormat: String?, input: String?): String {
        return try {
            val c = calendar(inputFormat, input)
            if (c != null) {
                dateString(outputFormat, c)
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun dbDate(): String {
        return dateString(DATE_DB_DATE, Calendar.getInstance())
    }

    fun dbTime(): String {
        return dateString(DATE_DB_TIME_HH_mm_ss_sss, Calendar.getInstance())
    }

    fun dayDifCount(previous: Calendar, later: Calendar): Int{
        val millis: Long = 24*60*60*1000L
        val c11 = Calendar.getInstance().apply {
            set(
                previous.get(Calendar.YEAR),
                previous.get(Calendar.MONTH),
                previous.get(Calendar.DATE),
            )
        }
        val c21 = Calendar.getInstance().apply {
            set(
                later.get(Calendar.YEAR),
                later.get(Calendar.MONTH),
                later.get(Calendar.DATE),
            )
        }

        val difMillis = c21.timeInMillis - c11.timeInMillis
        val dayDif = difMillis/millis

        return dayDif.toInt()
    }
}