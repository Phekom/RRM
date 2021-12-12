package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.provider

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.ImagePicker
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.ImagePickerActivity
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.util.IntentUtils
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.io.File
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.newpac.Image
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.newpac.ImageAdapter


/**
 * Created by Francis Mahlava on 2021/11/23.
 */
class GalleryProvider(activity: ImagePickerActivity) :
    BaseProvider(activity) {

    companion object {
        private const val GALLERY_INTENT_REQ_CODE = 4261
    }

    // Mime types restrictions for gallery. By default all mime types are valid
    private val mimeTypes: Array<String>
    private var adapter: ImageAdapter? = null
    private var images = java.util.ArrayList<Image>()


    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get MIME types
        mimeTypes = bundle.getStringArray(ImagePicker.EXTRA_MIME_TYPES) ?: emptyArray()
    }

    /**
     * Start Gallery Capture Intent
     */
    fun startIntent() {
        startGalleryIntent()
    }

    /**
     * Start Gallery Intent
     */

    private fun startGalleryIntent() {
        Coroutines.main {

//            OpenGalleryFromFolder(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)



            val uri = activity.photoUtil.getUriFromPath(activity.photoUtil.pictureFolder.toString())
           // getFilesFromDir(activity.photoUtil.pictureFolder)
//            getFilesFromDir(activity.photoUtil.pictureFolder)
            val galleryIntent = IntentUtils.getGalleryIntent(activity, mimeTypes,activity.photoUtil.pictureFolder )
            activity.startActivityForResult(galleryIntent, GALLERY_INTENT_REQ_CODE)
        }
    }


    fun OpenGalleryFromFolder(externalFilesDir: File): Boolean {
        val filePath = "content://za.co.xisystems.itismaintenance.qa.provider/root/Android/data/za.co.xisystems.itismaintenance.qa/files/Pictures/"
            //activity.photoUtil.pictureFolder.toString()
           // Environment.getExternalStorageDirectory().path + "/Pictures/" //+ folderName + "/"context: Context, folderName: String
        return OpenGalleryFromPathToFolder(activity, filePath)
    }

    // Finds the first image in the specified folder and uses it to open a the devices native gallery app with all images in that folder.
    private fun OpenGalleryFromPathToFolder(context: Context, folderPath: String?): Boolean {
        val folder = File(folderPath)
        val allFiles = folder.listFiles()
        if (allFiles != null && allFiles.size > 0) {
            val imageInFolder = getImageContentUri(context, allFiles[0])
            if (imageInFolder != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = imageInFolder
                context.startActivity(intent)
                return true
            }
        }
        return false
    }

    // converts the absolute path of a file to a content path
    // absolute path example: /storage/emulated/0/Pictures/folderName/Image1.jpg
    // content path example: content://media/external/images/media/47560

    @SuppressLint("Recycle", "Range")
    private fun getImageContentUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }


    fun getFilesFromDir(aStartingDir: File): List<File>? {
        val result: MutableList<File> = ArrayList()
        val filesAndDirs = aStartingDir.listFiles()
        val filesDirs: List<*> = listOf(filesAndDirs)
        val filesIter = filesDirs.iterator()
        var file: File? = null
        while (filesIter.hasNext()) {
            file = filesIter.next() as File?
            result.add(file!!) //always add, even if directory
            if (!file.isFile) {
                //must be a directory
                //recursive call!
//                val deeperList: List<*> = getFileListing(file)
//                result.addAll(deeperList)
            }
        }
        result.sort()
        return result
    }

    /**
     * Handle Gallery Intent Activity Result
     *
     * @param requestCode It must be {@link GalleryProvider#GALLERY_INTENT_REQ_CODE}
     * @param resultCode For success it should be {@link Activity#RESULT_OK}
     * @param data Result Intent
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY_INTENT_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(data)
            } else {
                setResultCancel()
            }
        }
    }

    /**
     * This method will be called when final result fot this provider is enabled.
     */
    private fun handleResult(data: Intent?) {
        val uri = data?.data
        if (uri != null) {
            takePersistableUriPermission(uri)
            activity.setImage(uri)
        } else {
            setError(R.string.error_failed_pick_gallery_image)
        }
    }

    /**
     * Take a persistable URI permission grant that has been offered. Once
     * taken, the permission grant will be remembered across device reboots.
     */
    private fun takePersistableUriPermission(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
