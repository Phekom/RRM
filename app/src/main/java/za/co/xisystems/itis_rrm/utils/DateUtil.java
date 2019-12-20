package za.co.xisystems.itis_rrm.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Mauritz Mollentze on 2014/12/19.
 * Update by Pieter Jacobs during 2016/05, 2016/07.
 */
public class DateUtil {

    // region (Private Static Final Fields)

    private static final String TAG = DateUtil.class.getSimpleName();
    private static final String parsingIso8601DateTimeFailed = "Parsing ISO8601 datetime failed";
    private static final String timeZeros = " 00:00:00";
    private static final String emptyString = "";
    private static final String dash = "-";
    private static final String zero = "0";

    // endregion (Private Static Final Fields)

    // region (Private Static Fields)

    private static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DateFormat readableDateForm = new SimpleDateFormat("dd MMMM yyyy");
    // endregion (Private Static Fields)

    // region (Public Static Methods)

    public static Date StringToDate(String stringDate) {
        if (null == stringDate) return null;

        try {
            return iso8601Format.parse(stringDate);
        } catch (ParseException e) {
            Log.e(TAG, parsingIso8601DateTimeFailed, e);
            return null;
        }
    }

    public static String DateToString(Date date) {
        if (null == date) return null;
        return iso8601Format.format(date);
    }

    public static Date getCurrentDateTime() {
        Date date = new Date();
        return StringToDate(iso8601Format.format(date));
    }

    public static String toStringReadable(Date date) {
        return date == null ? null : readableDateForm.format(date);
    }

    public static String toStringReadable(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return toStringReadable(calendar.getTime());
    }

    public static Date CalendarItemsToDate(int year, int monthOfYear, int dayOfMonth) {
        String mDate;
        String mMon;

        if (dayOfMonth < 10)
            mDate = zero + dayOfMonth;
        else
            mDate = emptyString + dayOfMonth;

        if ((monthOfYear + 1) < 10)
            mMon = zero + (monthOfYear + 1);
        else
            mMon = emptyString + (monthOfYear + 1);

        return StringToDate(emptyString + year + dash + mMon + dash + mDate + timeZeros);
    }

    public static Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    // endregion (Public Static Methods)
}
