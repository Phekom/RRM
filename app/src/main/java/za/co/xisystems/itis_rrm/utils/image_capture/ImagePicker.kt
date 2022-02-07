package za.co.xisystems.itis_rrm.utils.image_capture

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import za.co.xisystems.itis_rrm.utils.image_capture.camera.CameraActivity
import za.co.xisystems.itis_rrm.utils.image_capture.helper.Constants
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig


/**
 * Created by Francis Mahlava on 2021/11/23.
 */

typealias ImagePickerCallback = (ArrayList<Image>) -> Unit

class ImagePickerLauncher(
    private val context: () -> Context,
    private val resultLauncher: ActivityResultLauncher<Intent>
) {
    fun launch(config: ImagePickerConfig = ImagePickerConfig()) {
        val intent = createImagePickerIntent(context(), config)
        resultLauncher.launch(intent)
    }

    companion object {
        fun createIntent(
            context: Context,
            config: ImagePickerConfig = ImagePickerConfig()
        ): Intent {
            return createImagePickerIntent(context, config)
        }

    }
}

private fun createImagePickerIntent(context: Context, config: ImagePickerConfig): Intent {
    val intent: Intent
    if (config.isCameraOnly) {
        intent = Intent(context, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    } else {
        intent = Intent(context, ImagePickerActivity::class.java)
    }
    intent.putExtra(Constants.EXTRA_CONFIG, config)
    return intent
}

fun getImages(data: Intent?): ArrayList<Image> {
    return if (data != null) data.getParcelableArrayListExtra(Constants.EXTRA_IMAGES)!!
    else arrayListOf()
}

fun Fragment.registerImagePicker(
    context: () -> Context = { requireContext() },
    callback: ImagePickerCallback
): ImagePickerLauncher {
    return ImagePickerLauncher(context, createLauncher(callback))
}

private fun Fragment.createLauncher(callback: ImagePickerCallback): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val images = getImages(it.data)
        callback(images)
    }
}


