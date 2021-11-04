/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/15 12:30 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.sanselan.ImageReadException
import org.apache.sanselan.ImageWriteException
import org.apache.sanselan.Sanselan
import org.apache.sanselan.common.IImageMetadata
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter
import org.apache.sanselan.formats.tiff.write.TiffOutputSet
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.constants.Constants.NINETY_DAYS
import za.co.xisystems.itis_rrm.constants.Constants.THIRTY_DAYS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality

class PhotoUtil private constructor(private var appContext: Context) {

    lateinit var pictureFolder: File

    companion object {
        private lateinit var instance: PhotoUtil
        private const val BMP_LOAD_FAILED = "Failed to load bitmap"

        private fun initInstance(context: Context): PhotoUtil {
            instance = PhotoUtil(context)
            Coroutines.io {
                instance.pictureFolder =
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                if (!instance.pictureFolder.exists()) {
                    instance.pictureFolder.mkdirs()
                }
            }.also { return instance }
        }

        fun getInstance(appContext: Context): PhotoUtil {
            return if (!this::instance.isInitialized) {
                Timber.d("Initializing PhotoUtil")
                initInstance(appContext)
            } else {
                instance
            }
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }
    }

    /**
     * Erase RRM-related photographs older than 90 days in production
     * and 30 days in the dev environment.
     * Note, this simply clears up space on the device - image data on
     * the server side is unaffected.
     * Called after each successful login
     * @return Job
     */
    fun cleanupDevice() = Coroutines.io {

        val daysToKeepThreshold = if (BuildConfig.DEBUG) {
            THIRTY_DAYS
        } else {
            NINETY_DAYS
        }

        val presentTime: Long = (Date().time)

        pictureFolder.walkTopDown()
            .filter { file -> file.isFile }
            .sortedBy { file -> file.lastModified() }
            .forEach { file ->
                val diff = presentTime - file.lastModified()
                if (diff > daysToKeepThreshold && file.isFile) {
                    Timber.d("${file.name} was deleted, it was $diff old.")
                    file.delete()
                } else {
                    /**
                     * Call off the search - sorting means the first file
                     * younger than our criteria and it's sorted siblings are
                     * safe to keep.
                     */
                    return@io
                }
            }
    }

    suspend fun photoExist(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val image =
            File(getPhotoPathFromExternalDirectory(fileName).path!!)
        return@withContext image.exists()
    }

    suspend fun getPhotoPathFromExternalDirectory(
        photoName: String
    ): Uri = withContext(Dispatchers.IO) {
        var pictureName = photoName
        pictureName =
            if (!pictureName.lowercase(Locale.ROOT)
                .contains(".jpg")
            ) "$pictureName.jpg" else pictureName
        val fileName =
            pictureFolder.toString().plus(File.separator)
                .plus(pictureName)
        val file = File(fileName)
        return@withContext Uri.fromFile(file)
    }

    suspend fun getPhotoBitmapFromFile(
        selectedImage: Uri?,
        photoQuality: PhotoQuality
    ): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val options = BitmapFactory.Options()
            options.inSampleSize = photoQuality.value
            val fileDescriptor: AssetFileDescriptor =
                selectedImage?.let {
                    appContext.contentResolver
                        .openAssetFileDescriptor(it, "r")
                } ?: throw IllegalArgumentException(BMP_LOAD_FAILED)
            val bm = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.fileDescriptor,
                null,
                options
            )
            fileDescriptor.close()
            bm
        } catch (e: IllegalArgumentException) {
            Timber.e(e, BMP_LOAD_FAILED)
            null
        } catch (e: FileNotFoundException) {
            Timber.e(e, BMP_LOAD_FAILED)
            null
        } catch (e: IOException) {
            Timber.e(e, BMP_LOAD_FAILED)
            null
        }
    }

    suspend fun getCompressedPhotoWithExifInfo(
        bitmap: Bitmap,
        fileName: String
    ): ByteArray = withContext(Dispatchers.IO) {
        var byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var data = byteArrayOutputStream.toByteArray()
        var outputSet: TiffOutputSet? = null
        var metadata: IImageMetadata? = null
        try {
            metadata = Sanselan.getMetadata(
                File(
                    getPhotoPathFromExternalDirectory(
                        fileName
                    ).path!!
                )
            )
        } catch (e: ImageReadException) {
            Timber.e(e, BMP_LOAD_FAILED)
        } catch (e: IOException) {
            Timber.e(e, BMP_LOAD_FAILED)
        }
        outputSet = extractJpegData(metadata, outputSet)
        if (null != outputSet) {
            try {
                byteArrayOutputStream.flush()
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            byteArrayOutputStream = ByteArrayOutputStream()
            val exifRewriter = ExifRewriter()
            try {
                exifRewriter.updateExifMetadataLossless(data, byteArrayOutputStream, outputSet)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ImageWriteException) {
                e.printStackTrace()
            } catch (e: ImageReadException) {
                e.printStackTrace()
            }
            data = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
        }
        return@withContext data
    }

    private fun extractJpegData(
        metadata: IImageMetadata?,
        outputSet: TiffOutputSet?
    ): TiffOutputSet? {
        var outputSet1 = outputSet
        val jpegMetadata = metadata as JpegImageMetadata?
        if (jpegMetadata != null) {
            val exif = jpegMetadata.exif
            if (exif != null) {
                try {
                    outputSet1 = exif.outputSet
                } catch (e: ImageWriteException) {
                    e.printStackTrace()
                }
            }
        }
        return outputSet1
    }

    /**
     * saveImageToInternalStorage
     * @param imageUri
     */
//    public static Map<String, String> saveImageToInternalStorage(Context context, Bitmap bitmap) {
    suspend fun saveImageToInternalStorage(
        imageUri: Uri
    ): HashMap<String, String>? = withContext(Dispatchers.IO) {
        var scaledUri = imageUri
        return@withContext try {
            lateinit var scaledBitmap: Bitmap
            val options = BitmapFactory.Options()
            lateinit var result: String
            // if uri is content
            if (scaledUri.scheme == "content") {
                val cursor =
                    appContext.contentResolver.query(scaledUri, null, null, null, null)

                cursor?.run {
                    try {
                        when {
                            cursor.moveToFirst() -> { // local filesystem
                                var index = cursor.getColumnIndex("_data")
                                if (index == -1) { // google drive
                                    index = cursor.getColumnIndex("_display_name")
                                }
                                result = cursor.getString(index)
                                scaledUri = if (result.isBlank()) return@withContext null else Uri.parse(result)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ërror loading photo $scaledUri")
                    } finally {
                        cursor.close()
                    }
                }
            }
            result = scaledUri.path ?: ""
            // get filename + ext of path
            val cut = result.lastIndexOf('/')
            if (cut != -1) result = result.substring(cut + 1)
            val imageFileName = result
            val direct =
                File(pictureFolder.toString())
            if (!direct.exists()) {
                direct.mkdirs()
            }
            val path = direct.toString() + File.separator + imageFileName
            options.inJustDecodeBounds = true
            var bmp = BitmapFactory.decodeFile(path, options)
            var actualHeight = options.outHeight
            var actualWidth = options.outWidth
            //      max Height and width values of the compressed image is taken as 816x612
            val pair = resizeBitmapToMax(actualWidth, actualHeight)
            actualHeight = pair.first
            actualWidth = pair.second
            //      setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize =
                calculateInSampleSize(options, actualWidth, actualHeight)
            //      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false
            //      this options allow android to claim the bitmap memory if it runs low on memory

            // these aren't much use for decodeFile operations
            // options.inPurgeable = true
            // options.inInputShareable = true

            options.inTempStorage = ByteArray(16 * 1024)
            try { //          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(path, options)

                scaledBitmap =
                    Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
            } catch (exception: OutOfMemoryError) {
                Timber.e(exception, "Failed to create bitmap")
            }

            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(
                bmp,
                middleX - bmp.width / 2,
                middleY - bmp.height / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            //      check the rotation of the image and display it properly
            scaledBitmap = applyExifRotation(path, scaledBitmap)
            val out: FileOutputStream?
            try {
                out = FileOutputStream(path)
                // write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            val map: HashMap<String, String> =
                HashMap()
            map["filename"] = imageFileName
            map["path"] = path
            map
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e, "error saving photo: $e")
            null
        }
    }

    private fun applyExifRotation(
        path: String,
        scaledBitmap: Bitmap
    ): Bitmap {
        var scaledBitmap1 = scaledBitmap
        val exif: ExifInterface
        try {
            exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Timber.d("Exif: initial $orientation")
            val matrix = Matrix()
            val degrees = exifToDegrees(orientation).toFloat()
            matrix.postRotate(degrees)
            Timber.d("Exif: $orientation")

            scaledBitmap1 = Bitmap.createBitmap(
                scaledBitmap1,
                0,
                0,
                scaledBitmap1.width,
                scaledBitmap1.height,
                matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return scaledBitmap1
    }

    private fun resizeBitmapToMax(
        actualWidth: Int,
        actualHeight: Int
    ): Pair<Int, Int> {
        var actualWidth1 = actualWidth
        var actualHeight1 = actualHeight
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth1 / actualHeight1).toFloat()
        val maxRatio = maxWidth / maxHeight
        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight1 > maxHeight || actualWidth1 > maxWidth) {
            when {
                imgRatio < maxRatio -> {
                    imgRatio = maxHeight / actualHeight1
                    actualWidth1 = (imgRatio * actualWidth1).toInt()
                    actualHeight1 = maxHeight.toInt()
                }
                imgRatio > maxRatio -> {
                    imgRatio = maxWidth / actualWidth1
                    actualHeight1 = (imgRatio * actualHeight1).toInt()
                    actualWidth1 = maxWidth.toInt()
                }
                else -> {
                    actualHeight1 = maxHeight.toInt()
                    actualWidth1 = maxWidth.toInt()
                }
            }
        }
        return Pair(actualHeight1, actualWidth1)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        actualWidth: Int,
        actualHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > actualHeight || width > actualWidth) {
            val heightRatio =
                (height.toFloat() / actualHeight.toFloat()).roundToLong()
            val widthRatio =
                (width.toFloat() / actualWidth.toFloat()).roundToLong()
            inSampleSize = (if (heightRatio < widthRatio) heightRatio else widthRatio).toInt()
        }
        val totalPixels = width * height.toFloat()
        val totalReqPixelsCap = actualWidth * actualHeight * 2.toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    suspend fun deleteImageFile(
        imagePath: String?
    ): Boolean = withContext(Dispatchers.IO) { // Get the file
        return@withContext if (imagePath != null) {
            val imageFile = File(imagePath)
            // Delete the image
            val deleted = imageFile.delete()
            // If there is an error deleting the file, show a Toast
            if (!deleted) {
                Timber.e("$imagePath was not deleted")
            }
            deleted
        } else {
            true
        }
    }

    fun persistImageToLocal(photo: String, fileName: String) = Coroutines.io {
        val imageByteArray: ByteArray = decode64Pic(photo)
        File(pictureFolder.path + "/" + fileName).writeBytes(imageByteArray)
    }

    /**
     * Encode bitmap array to a Base64 string for transmission to the backend.
     */
    fun encode64Pic(photo: ByteArray): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                Base64.getEncoder().encodeToString(photo)
            else ->
                // Fallback for pre-Marshmallow
                android.util.Base64.encodeToString(photo, android.util.Base64.DEFAULT)
        }
    }

    private fun decode64Pic(photo: String): ByteArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(photo)
        } else {
            android.util.Base64.decode(photo, android.util.Base64.DEFAULT)
        }
    }

    private val authority = BuildConfig.APPLICATION_ID + ".provider"

    fun getUri(photoPath: String): Uri? {
        return try {
            FileProvider.getUriForFile(
                appContext, authority,
                File(photoPath)
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Could not load photo: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}")
            null
        } catch (e: IOException) {
            Timber.e(e, "Could not load photo: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}")
            null
        }
    }

    private suspend fun getUriFromPath(filePath: String): Uri? = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(filePath)
            val uri = Uri.fromFile(file)
            uri
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract image Uri: ${e.message}")
            null
        }
    }

    suspend fun prepareGalleryPairs(filenames: List<String>): List<Pair<Uri, Bitmap>> =
        withContext(Dispatchers.IO) {
            val photoQuality = when (filenames.size) {
                in 1..4 -> PhotoQuality.HIGH
                in 5..16 -> PhotoQuality.MEDIUM
                else -> PhotoQuality.THUMB
            }
            return@withContext filenames.mapNotNull { path ->
                try {
                    val uri = getUriFromPath(path)

                    val bmp = uri?.let {
                        getPhotoBitmapFromFile(
                            it,
                            photoQuality
                        )
                    }
                    Pair(uri!!, bmp!!)
                } catch (t: Throwable) {
                    val message = "Failed to create gallery image: " +
                        (t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    Timber.e(t, message)
                    null
                }
            }
        }

    suspend fun getUri(): Uri? = withContext(Dispatchers.IO) {
        try {
            return@withContext FileProvider.getUriForFile(
                appContext,
                authority,
                createImageFile()!!
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Failed to create URI: ${e.message}")
            return@withContext null
        }
    }

    @Throws(IOException::class)
    private suspend fun createImageFile(): File? = withContext(Dispatchers.IO) {
        val imageFileName = UUID.randomUUID()
        return@withContext try {
            File(pictureFolder, "$imageFileName.jpg")
        } catch (ex: IOException) {
            Timber.e(ex, "Failed to create image file $imageFileName.jpg")
            null
        }
    }
}
