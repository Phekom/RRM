package za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension

import java.io.File

/**
 * Created by Francis.Mahlava on 2022/04/24.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

fun File.getSize(size: Size = Size.MB): String {
    val lengthBytes = this.length()
    return when (size) {
        Size.B -> "$lengthBytes"
        Size.KB -> "${lengthBytes / 1024} KB"
        Size.MB -> {
            val str = (lengthBytes / 1024).toString()
            return when (str.length) {
                1 -> "0.00$str MB"
                2 -> "0.0$str MB"
                3 -> "0.$str MB"
                else -> "${str.substring(0, str.length - 3)}.${str.substring(str.length - 3,
                        str.length)} MB"
            }
        }
    }
}

enum class Size { B, KB, MB }