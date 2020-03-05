package za.co.xisystems.itis_rrm.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Francis Mahlava on 2020/02/11.
 */
class DateDeserializer : JsonDeserializer<Date?> {
    @Throws(JsonParseException::class)
    override fun deserialize(element: JsonElement, arg1: Type?, arg2: JsonDeserializationContext?): Date? {
        val date = element.asString
        val formatter = SimpleDateFormat("M/d/yy hh:mm a")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            formatter.parse(date)
        } catch (e: ParseException) {
//            System.err.println("Failed to parse Date due to:", e)
            null
        }
    }
}