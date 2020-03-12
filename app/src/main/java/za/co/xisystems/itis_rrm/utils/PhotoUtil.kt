package za.co.xisystems.itis_rrm.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import org.apache.sanselan.ImageReadException
import org.apache.sanselan.ImageWriteException
import org.apache.sanselan.Sanselan
import org.apache.sanselan.common.IImageMetadata
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter
import org.apache.sanselan.formats.tiff.write.TiffOutputSet
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.EstimatePhotoFragment
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure.CaptureItemMeasurePhotoActivity
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong

object PhotoUtil {
    const val FOLDER = "ITIS_RRM_Photos"
//    const val FOLDER = "ITISMaintenancePhotos"
@SuppressLint("SimpleDateFormat")
private val ISO_8601_FORMAT: DateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun dateToString(date: Date?): String {
        return ISO_8601_FORMAT.format(date)
    }

    fun getPhotoBitMapFromFile(
        context: Context,
        selectedImage: Uri?,
        photoQuality: PhotoQuality
    ): Bitmap? {
        var bm: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inSampleSize = photoQuality.value
        var fileDescriptor: AssetFileDescriptor? = null
        // TODO improve this try-catch-finally
        try {
            fileDescriptor =
                context.contentResolver.openAssetFileDescriptor(selectedImage, "r")
            if (fileDescriptor != null) bm = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.fileDescriptor,
                null,
                options
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            if (fileDescriptor != null) try {
                fileDescriptor.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bm
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getBitmapSize(bitmap: Bitmap): Int {
        return bitmap.allocationByteCount / 1024
    }

    fun getRotationForImage(path: Uri): Int {
        var rotation = 0
        try {
            val exif = ExifInterface(path.path)
            rotation = exifToDegrees(
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotation
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    fun createPhoto(fileName: String, photoByteArray: ByteArray?) {
        val imagesFolder =
            File(Environment.getExternalStorageDirectory(), FOLDER)
        imagesFolder.mkdirs()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imagesFolder.path + "/" + fileName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            if (out != null) {
                out.write(photoByteArray)
                out.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun cleanupDevice(fileNames: ArrayList<String?>) {
        for (fileName in fileNames) {
            val file = File(fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    fun photoExist(fileName: String): Boolean {
        val image =
            File(getPhotoPathFromExternalDirectory(null, fileName).path)
        return image.exists()
    }

    fun getPhotoPathFromExternalDirectory(
        context: Context?,
        photoName: String
    ): Uri {
        var photoName = photoName
        photoName =
            if (!photoName.toLowerCase(Locale.ROOT)
                    .contains(".jpg")
            ) "$photoName.jpg" else photoName
        var fileName =
            Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER + File.separator + photoName
        var file = File(fileName)
        if (!file.exists()) { //  if not in the folder then go to the PhotosDirectory to check if in their.
            fileName =
                Environment.getExternalStorageDirectory().toString() + File.separator + photoName
            file = File(fileName)
        }
        return Uri.fromFile(file)
    }

    fun getPhotoBitmapFromFile(
        context: Context,
        selectedImage: Uri?,
        photoQuality: PhotoQuality
    ): Bitmap? {
        val width = 0
        val height = 0
        var bm: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            if (selectedImage != null) {
                options.inSampleSize = photoQuality.value
                var fileDescriptor: AssetFileDescriptor? = null
                try {
                    fileDescriptor =
                        context.contentResolver.openAssetFileDescriptor(selectedImage, "r")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } finally {
                    try {
                        assert(fileDescriptor != null)
                        bm = BitmapFactory.decodeFileDescriptor(
                            fileDescriptor!!.fileDescriptor,
                            null,
                            options
                        )
                        fileDescriptor.close()
                    } catch (e: IOException) {
                        bm = null
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bm
    }

    fun getCompressedPhotoWithExifInfo(
        context: Context?,
        bitmap: Bitmap,
        fileName: String
    ): ByteArray {
        var byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        var outputSet: TiffOutputSet? = null
        var metadata: IImageMetadata? = null
        try {
            metadata = Sanselan.getMetadata(
                File(
                    getPhotoPathFromExternalDirectory(
                        null,
                        fileName
                    ).path
                )
            )
        } catch (e: ImageReadException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val jpegMetadata = metadata as JpegImageMetadata?
        if (jpegMetadata != null) {
            val exif = jpegMetadata.exif
            if (exif != null) {
                try {
                    outputSet = exif.outputSet
                } catch (e: ImageWriteException) {
                    e.printStackTrace()
                }
            }
        }
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
            return byteArrayOutputStream.toByteArray()
        }
        return data
    }
    //=============================================================================================================================================================
    /**
     * saveImageToInternalStorage
     * Todo Make Image Quality Better
     * @param imageUri
     */
//    public static Map<String, String> saveImageToInternalStorage(Context context, Bitmap bitmap) {
    fun saveImageToInternalStorage(
        context: Context,
        imageUri: Uri
    ): Map<String, String?>? {
        var imageUri = imageUri
        return try {
            var scaledBitmap: Bitmap? = null
            val options = BitmapFactory.Options()
            var result: String?
            //if uri is content
            if (imageUri.scheme != null && imageUri.scheme == "content") {
                val cursor =
                    context.contentResolver.query(imageUri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) { //local filesystem
                        var index = cursor.getColumnIndex("_data")
                        if (index == -1) //google drive
                            index = cursor.getColumnIndex("_display_name")
                        result = cursor.getString(index)
                        imageUri = if (result != null) Uri.parse(result) else return null
                    }
                } finally {
                    cursor!!.close()
                }
            }
            result = imageUri.path
            //get filename + ext of path
            val cut = result.lastIndexOf('/')
            if (cut != -1) result = result.substring(cut + 1)
            val imageFileName = result
            val direct =
                File(Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER)
            if (!direct.exists()) {
                direct.mkdirs()
            }
            val path = direct.toString() + File.separator + imageFileName
            options.inJustDecodeBounds = true
            var bmp = BitmapFactory.decodeFile(path, options)
            var actualHeight = options.outHeight
            var actualWidth = options.outWidth
            //      max Height and width values of the compressed image is taken as 816x612
            val maxHeight = 816.0f
            val maxWidth = 612.0f
            var imgRatio = actualWidth / actualHeight.toFloat()
            val maxRatio = maxWidth / maxHeight
            //      width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                when {
                    imgRatio < maxRatio -> {
                        imgRatio = maxHeight / actualHeight
                        actualWidth = (imgRatio * actualWidth).toInt()
                        actualHeight = maxHeight.toInt()
                    }
                    imgRatio > maxRatio -> {
                        imgRatio = maxWidth / actualWidth
                        actualHeight = (imgRatio * actualHeight).toInt()
                        actualWidth = maxWidth.toInt()
                    }
                    else -> {
                        actualHeight = maxHeight.toInt()
                        actualWidth = maxWidth.toInt()
                    }
                }
            }
            //      setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize =
                calculateInSampleSize(options, actualWidth, actualHeight)
            //      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false
            //      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true
            options.inInputShareable = true
            options.inTempStorage = ByteArray(16 * 1024)
            try { //          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(path, options)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
            }
            try {
                scaledBitmap =
                    Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
            }
            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap)
            canvas.matrix = scaleMatrix
            canvas.drawBitmap(
                bmp,
                middleX - bmp.width / 2,
                middleY - bmp.height / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            //      check the rotation of the image and display it properly
            val exif: ExifInterface
            try {
                exif = ExifInterface(path)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0
                )
                Log.d("EXIF", "Exif: $orientation")
                val matrix = Matrix()
                when (orientation) {
                    6 -> {
                        matrix.postRotate(90f)
                        Log.d("EXIF", "Exif: $orientation")
                    }
                    3 -> {
                        matrix.postRotate(180f)
                        Log.d("EXIF", "Exif: $orientation")
                    }
                    8 -> {
                        matrix.postRotate(270f)
                        Log.d("EXIF", "Exif: $orientation")
                    }
                }
                scaledBitmap = Bitmap.createBitmap(
                    scaledBitmap, 0, 0,
                    scaledBitmap!!.width, scaledBitmap.height, matrix,
                    true
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(path)
                //          write the compressed bitmap at the destination specified by filename.
                scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            val map: MutableMap<String, String?> =
                HashMap()
            map["filename"] = imageFileName
            map["path"] = path
            map
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("x-", "error saving photo: $e")
            null
        }
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

    fun getUri(estimatePhotoFragment: EstimatePhotoFragment?): Uri? {
        try {
            return FileProvider.getUriForFile(
                estimatePhotoFragment?.context!!.applicationContext, BuildConfig.APPLICATION_ID + ".provider",
                createImageFile()
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deleteImageFile(
        context: Context,
        imagePath: String?
    ): Boolean { // Get the file
        val imageFile = File(imagePath)
        // Delete the image
        val deleted = imageFile.delete()
        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            val errorMessage = context.getString(R.string.error)
        }
        return deleted
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val uuid = UUID.randomUUID()
        val imageFileName = uuid.toString()
        //        String imageFileName = "010";
        val storageDir =
            File(Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        //        File image = File.createTempFile(
//               "111" ,imageFileName + ".jpg",  storageDir);
        return File(storageDir, "$imageFileName.jpg")
    } //    public static void createFile(String fileName, byte[] bytes) throws IOException {photoByteArray: ByteArray?,

    fun createPhotoFolder(photo: String, fileName: String) {

        val storageDir =
            File(Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }


        val imageByteArray: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(photo)
        } else {
            android.util.Base64.decode(photo, android.util.Base64.DEFAULT)
        }

        File(storageDir.path + "/" + fileName).writeBytes(imageByteArray)
    }


    fun createPhotoFolder() {

        val storageDir =
            File(Environment.getExternalStorageDirectory().toString() + File.separator + FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

//    fun getUri2(captureItemMeasurePhotoActivity: CaptureItemMeasurePhotoActivity): Uri? {
//        try {
//            return FileProvider.getUriForFile(
//                captureItemMeasurePhotoActivity, BuildConfig.APPLICATION_ID + ".provider",
//                createImageFile()
//            )
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return null
//    }


    fun getUri3(context : Context): Uri? {
        try {
            return FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".provider",
                createImageFile()
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun setRouteSecPoint(direction: String, linearId: String, pointLocation: Double, sectionId: Int, projectId: String?){
          val direction = direction
          val linearId =  linearId
          val pointLocation =  pointLocation
        val  sectionId =  sectionId
        val  projectId = projectId
    }


}