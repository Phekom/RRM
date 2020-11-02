package za.co.xisystems.itis_rrm.data._commons.utils

import java.util.UUID
import za.co.xisystems.itis_rrm.utils.DataConversion

object SqlLitUtils {
    fun generateUuid(): String {
        return DataConversion.removeDashesAndUppercaseString(UUID.randomUUID().toString())!!
    }
}
