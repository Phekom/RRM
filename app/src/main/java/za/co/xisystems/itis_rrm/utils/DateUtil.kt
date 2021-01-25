/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.utils

import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Created by Mauritz Mollentze on 2014/12/19.
 * Update by Pieter Jacobs during 2016/05, 2016/07.
 */
object DateUtil {
    // region (Private Static Final Fields)
    private val TAG = DateUtil::class.java.simpleName
    private const val parsingIso8601DateTimeFailed = "Parsing ISO8601 datetime failed"
    private const val timeZeros = " 00:00:00"
    private const val emptyString = ""
    private const val dash = "-"
    private const val zero = "0"

    // endregion (Private Static Final Fields)"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
// region (Private Static Fields)SimpleDateFormat("yyyy-MM-dd HH:mm:ss") as DateFormat
    private val iso8601Format: DateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

//    private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    private val readableDateForm: DateFormat = SimpleDateFormat("dd MMMM yyyy")

    // endregion (Private Static Fields)
// region (Public Static Methods)
    fun StringToDate(stringDate: String?): Date? {
        return if (null == stringDate) null else try {
            iso8601Format.parse(stringDate)
        } catch (e: ParseException) {
            Timber.e(e, parsingIso8601DateTimeFailed)
            null
        }
    }

    fun DateToString(date: Date?): String? {
        return if (null == date) null else iso8601Format.format(date)
    }

    val currentDateTime: Date?
        get() {
            val date = Date()
            return StringToDate(iso8601Format.format(date))
        }

    val currentDate: Date?
        get() {
            val date = Date()
            return StringToDate(readableDateForm.format(date))
        }

    fun toStringReadable(date: Date?): String? {
        return if (date == null) null else readableDateForm.format(date)
    }

    fun toStringReadable(year: Int, month: Int, day: Int): String? {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        return toStringReadable(calendar.time)
    }

    fun CalendarItemsToDate(year: Int, monthOfYear: Int, dayOfMonth: Int): Date? {
        val mDate: String = if (dayOfMonth < 10) zero + dayOfMonth else emptyString + dayOfMonth
        val mMon: String =
            if (monthOfYear + 1 < 10) zero + (monthOfYear + 1) else emptyString + (monthOfYear + 1)
        return StringToDate(emptyString + year + dash + mMon + dash + mDate + timeZeros)
    }

    fun DateToCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal
    } // endregion
}
