package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import za.co.xisystems.itis_rrm.R
import java.io.File

/**
 * Created by Francis Mahlava on 2021/11/23.
 */


object IntentUtils {

    /**
     * @return Intent Gallery Intent
     */
    @JvmStatic
    fun getGalleryIntent(context: Context, mimeTypes: Array<String>, pictureFolderUri: File): Intent {


////        if (pictureFolder.exists()){
//            val allFiles: Array<File> = pictureFolder.listFiles {
//                    dir, name -> name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") }
////        }
        val intent = getGalleryDocumentIntent(mimeTypes, pictureFolderUri)
        if (intent.resolveActivity(context.packageManager) != null) {
            return intent
        }
        return getLegacyGalleryPickIntent(mimeTypes,pictureFolderUri)
    }



    /**
     * Ref: https://developer.android.com/reference/android/content/Intent#FLAG_GRANT_PERSISTABLE_URI_PERMISSION
     *
     * @return Intent Gallery Document Intent
     */
    private fun getGalleryDocumentIntent(mimeTypes: Array<String>, pictureFolderUri: File): Intent {
        // Show Document Intent
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).applyImageTypes(mimeTypes, pictureFolderUri)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    /**
     * @return Intent Gallery Pick Intent
     */
    private fun getLegacyGalleryPickIntent(mimeTypes: Array<String>, pictureFolderUri: File): Intent {
        // Show Gallery Intent, Will open google photos
        return Intent( Intent.ACTION_PICK)
            .applyImageTypes(mimeTypes,pictureFolderUri)
    }

    private fun Intent.applyImageTypes(mimeTypes: Array<String>, pictureFolderUri: File): Intent {
        // Apply filter to show image only in intent
        type = "image/*"
        if (mimeTypes.isNotEmpty()) {
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        return this
    }

    /**
     * @return Intent Camera Intent
     */
    @JvmStatic
    fun getCameraIntent(context: Context, imageUri : Uri?): Intent? {
//        fun getCameraIntent(context: Context, file: File): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val authority = context.packageName + context.getString(R.string.image_picker_provider_authority_suffix)
//            val photoURI = FileProvider.getUriForFile(context, authority, file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)//Uri.fromFile(file))
        }

        return intent
    }

    /**
     * Check if Camera App is available or not
     *
     * @return true if Camera App is Available else return false
     */
    @JvmStatic
    fun isCameraAppAvailable(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * Get Intent to View Uri backed File
     *
     * @param context
     * @param uri
     * @return Intent
     */
    @JvmStatic
    fun getUriViewIntent(context: Context, uri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val authority =
            context.packageName + context.getString(R.string.image_picker_provider_authority_suffix)

        val file = DocumentFile.fromSingleUri(context, uri)
        val dataUri = if (file?.canRead() == true) {
            uri
        } else {
            val filePath = FileUriUtils.getRealPath(context, uri)!!
            FileProvider.getUriForFile(context, authority, File(filePath))
        }

        intent.setDataAndType(dataUri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return intent
    }
}
