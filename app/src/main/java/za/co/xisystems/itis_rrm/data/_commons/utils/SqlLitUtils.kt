package za.co.xisystems.itis_rrm.data._commons.utils

import za.co.xisystems.itis_rrm.utils.DataConversion
import java.util.*

object SqlLitUtils {
    fun generateUuid(): String {
        return DataConversion.removeDashesAndUppercaseString(UUID.randomUUID().toString())!!
    }
}
