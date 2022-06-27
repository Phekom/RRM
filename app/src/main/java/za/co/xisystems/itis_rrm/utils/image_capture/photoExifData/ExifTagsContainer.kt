package za.co.xisystems.itis_rrm.utils.image_capture.photoExifData
/**
 * Created by Francis.Mahlava on 2022/04/24.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

data class ExifTagsContainer(val list: List<ExifField>, val type: Type) {
    fun getOnStringProperties(): String = when {
        list.isEmpty() -> "No data provided"
        else -> {
            val s = StringBuilder()
            list.forEach { s.append("${it.tag}: ${it.attribute}\n") }
            s.toString().substring(0, s.length - 1)
        }
    }
}

enum class Type { GPS, DATE, CAMERA_PROPERTIES, DIMENSION, OTHER }
