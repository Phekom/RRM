/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.utils

import androidx.room.TypeConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDatetimeConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return when {
            !value.isNullOrEmpty() -> {
                formatter.parse(value, OffsetDateTime::from)
            }
            else -> {
                null
            }
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return when {
            null != date -> {
                date.format(formatter)
            }
            else -> {
                null
            }
        }
    }
}
