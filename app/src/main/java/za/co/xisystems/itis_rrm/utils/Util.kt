package za.co.xisystems.itis_rrm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import za.co.xisystems.itis_rrm.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

object Util {
    fun toByteArray(stringUUID: String?): ByteArray? {
        var stringUUID: String? = stringUUID
        return if (stringUUID != null) {
            if (!stringUUID.contains("-")) stringUUID =
                insertDashUUID(stringUUID)
            val uuid = UUID.fromString(stringUUID.toLowerCase(Locale.ROOT))
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            bb.array()
        } else {
            null
        }
    }

    fun disableDoubleClick(view: View) {
        view.isEnabled = false
        view.postDelayed({ view.isEnabled = true }, 500)
    }

    fun preventDoubleClick(view: View) {
        disableDoubleClick(view)
    }

    fun insertDashUUID(uuid: String?): String {
        var sb = StringBuffer(uuid)
        sb.insert(8, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(13, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(18, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }

    fun ByteArrayToStringUUID(bytes: ByteArray?): String? { //        if (isStub()) {
//            String uud = UUID.randomUUID().toString();
//            return removeDashesAndUppercaseString(uud);
//        }
        val bb = ByteBuffer.wrap(bytes)
        val high = bb.long
        val low = bb.long
        val uuid = UUID(high, low)
        return removeDashesAndUppercaseString(uuid.toString())
    }

    fun removeDashesAndUppercaseString(string: String?): String? {
        return string?.toUpperCase(Locale.ROOT)?.replace("-", "")
    }

    fun getUri(context: Context?): Uri? {
        try {
            return FileProvider.getUriForFile(
                context!!,
                BuildConfig.APPLICATION_ID + ".provider",
                createImageFile()
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "Camera"
        )
        return File.createTempFile(
            imageFileName, ".jpg", storageDir
        )
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos)
        return baos.toByteArray()
    }

    fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    fun arrayToString(array: Array<String?>): String {
        if (array.size == 0) return ""
        val sb = StringBuilder()
        for (string in array) {
            sb.append(",'").append(string).append("'")
        }
        return sb.substring(1)
    }
}