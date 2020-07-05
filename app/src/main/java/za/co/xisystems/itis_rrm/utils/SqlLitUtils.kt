package za.co.xisystems.itis_rrm.utils

import java.util.*

object SqlLitUtils {
    fun generateUuid(): String {
        return DataConversion.removeDashesAndUppercaseString(UUID.randomUUID().toString())!!
    }
}
