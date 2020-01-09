package za.co.xisystems.itis_rrm.utils

import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
    // endregion (Private Static Final Fields)
// region (Private Static Fields)
    private val iso8601Format: DateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val readableDateForm: DateFormat = SimpleDateFormat("dd MMMM yyyy")
    // endregion (Private Static Fields)
// region (Public Static Methods)
    fun StringToDate(stringDate: String?): Date? {
        return if (null == stringDate) null else try {
            iso8601Format.parse(stringDate)
        } catch (e: ParseException) {
            Log.e(TAG, parsingIso8601DateTimeFailed, e)
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

    fun toStringReadable(date: Date?): String? {
        return if (date == null) null else readableDateForm.format(date)
    }

    fun toStringReadable(year: Int, month: Int, day: Int): String? {
        val calendar = Calendar.getInstance()
        calendar[year, month] = day
        return toStringReadable(calendar.time)
    }

    fun CalendarItemsToDate(year: Int, monthOfYear: Int, dayOfMonth: Int): Date? {
        val mDate: String
        val mMon: String
        mDate = if (dayOfMonth < 10) zero + dayOfMonth else emptyString + dayOfMonth
        mMon =
            if (monthOfYear + 1 < 10) zero + (monthOfYear + 1) else emptyString + (monthOfYear + 1)
        return StringToDate(emptyString + year + dash + mMon + dash + mDate + timeZeros)
    }

    fun DateToCalendar(date: Date?): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal
    } // endregion (Public Static Methods)
}