package za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
/**
 * Created by Francis.Mahlava on 2022/04/24.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

fun Uri.getPathFromUri(context: Context): String? {
    var filePath: String? = null
    val contentResolver = context.contentResolver

    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = contentResolver.query(this, proj, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = cursor.getString(column_index)
        }
    }
    cursor?.close()

    return filePath
}
