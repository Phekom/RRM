package za.co.xisystems.itis_rrm.services

import android.content.Context
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.LocationException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.utils.Utils.round

@SuppressWarnings("MagicNumber")
class OutsideSanralReserveException
    (latitude: Double, longitude: Double, message: String? = null, context: Context? = null) : Throwable(message) {
    init {
        if (message.isNullOrBlank() && context != null) {
            throw IllegalArgumentException("Please specify either a message or a context.")
        }
        if (message.isNullOrBlank() && context != null) {
            val newMessage = context.resources.getString(
                R.string.not_nra_territory,
                latitude.round(6), longitude.round(6)
            )
            throw LocationException(newMessage)
        } else {
            throw LocationException(message ?: XIErrorHandler.UNKNOWN_ERROR)
        }
    }
}

class OutsideCurrentProjectException(message: String? = null, latitude: Double, longitude: Double, context: Context) : Throwable(message) {
    init {
    }
}
