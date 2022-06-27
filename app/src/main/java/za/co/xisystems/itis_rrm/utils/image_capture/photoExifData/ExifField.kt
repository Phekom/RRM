package za.co.xisystems.itis_rrm.utils.image_capture.photoExifData

/**
 * Created by Francis.Mahlava on 2022/04/24.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

data class ExifField(val tag: String, val attribute: String)

data class Location(val latitude: Double, val longitude: Double)