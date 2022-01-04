package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

object ExifDataCopier {

    @SuppressLint("LogNotTimber")
    fun copyExif(filePathOri: File, filePathDest: File) {
        try {
            val oldExif = ExifInterface(filePathOri)
            val newExif = ExifInterface(filePathDest)
            val attributes: List<String> = listOf(
                "FNumber",
                "ExposureTime",
                "ISOSpeedRatings",
                "GPSAltitude",
                "GPSAltitudeRef",
                "FocalLength",
                "GPSDateStamp",
                "WhiteBalance",
                "GPSProcessingMethod",
                "GPSTimeStamp",
                "DateTime",
                "Flash",
                "GPSLatitude",
                "GPSLatitudeRef",
                "GPSLongitude",
                "GPSLongitudeRef",
                "Make",
                "Model",
                "Orientation"
            )
            for (attribute in attributes) {
                setIfNotNull(oldExif, newExif, attribute)
            }
            newExif.saveAttributes()
        } catch (ex: Exception) {
            Log.e("ExifDataCopier", "Error preserving Exif data on selected image: $ex")
        }
    }

    private fun setIfNotNull(oldExif: ExifInterface, newExif: ExifInterface, property: String) {
        if (oldExif.getAttribute(property) != null) {
            newExif.setAttribute(property, oldExif.getAttribute(property))
        }
    }
}
