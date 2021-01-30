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
import java.util.Locale

/**
 * Created by Mauritz Mollentze on 2014/12/19.
 * Update by Pieter Jacobs during 2016/05, 2016/07.
 */
object DateUtil {
    private const val parsingIso8601DateTimeFailed = "Parsing ISO8601 datetime failed"

    private val iso8601Format: DateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT)

    private val readableDateForm: DateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ROOT)

    fun stringToDate(stringDate: String?): Date? {
        return if (null == stringDate) null else try {
            iso8601Format.parse(stringDate)
        } catch (e: ParseException) {
            Timber.e(e, parsingIso8601DateTimeFailed)
            null
        }
    }

    fun dateToString(date: Date?): String? {
        return if (null == date) null else iso8601Format.format(date)
    }

    val currentDateTime: Date?
        get() {
            val date = Date()
            return stringToDate(iso8601Format.format(date))
        }

    fun toStringReadable(date: Date?): String? {
        return if (date == null) null else readableDateForm.format(date)
    }

    fun toStringReadable(year: Int, month: Int, day: Int): String? {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        return toStringReadable(calendar.time)
    }
}
