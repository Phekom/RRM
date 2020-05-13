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
import za.co.xisystems.itis_rrm.data.localDB.entities.StringKeyValuePair
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    const val LAST_CONTRACT_REFRESH_TIMESTAMP = "last_contract_refresh_timestamp"
    const val LAST_WORKFLOW_REFRESH_TIMESTAMP = "last_workflow_refresh_timestamp"
    const val LAST_MAP_REFRESH_TIMESTAMP = "last_map_refresh_timestamp"
    const val MAX_RETRIES = 3L
    private const val INITIAL_BACKOFF = 2000L
    private var formatter = SimpleDateFormat("h:mm aa", Locale.getDefault())
    private var dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val df = DecimalFormat("###.##")

    fun toByteArray(mStringUUID: String?): ByteArray? {
        var stringUUID: String? = mStringUUID
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
        var sb = StringBuffer(uuid!!)
        sb.insert(8, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(13, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(18, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }

    fun byteArrayToStringUUID(bytes: ByteArray?): String? { //        if (isStub()) {
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
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
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
        if (array.isEmpty()) return ""
        val sb = StringBuilder()
        for (string in array) {
            sb.append(",'").append(string).append("'")
        }
        return sb.substring(1)
    }

    fun getTimeString(timeInMillis: Long): String {
        return formatter.format(Date(timeInMillis * 1000))
    }

    fun getDateString(timeInMillis: Long): String {
        return dateFormatter.format(Date(timeInMillis * 1000))
    }

    fun shouldCallApi(
        lastApiCallMillis: String,
        cacheThresholdInMillis: Long = 300000L //default value is 5 minutes//
    ): Boolean {
        return (System.currentTimeMillis() - lastApiCallMillis.toLong()) >= cacheThresholdInMillis
    }

    fun getCurrentTimeKeyValuePair(key: String): StringKeyValuePair {
        return StringKeyValuePair(key, System.currentTimeMillis().toString())
    }

    fun getBackoffDelay(attempt: Long) = INITIAL_BACKOFF * (attempt + 1)

}