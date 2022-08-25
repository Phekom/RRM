package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import za.co.xisystems.itis_rrm.R

object DeviceHelper {

    val isMinSdk29 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val isMinSdk26 get() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    fun checkCameraAvailability(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val isAvailable = intent.resolveActivity(context.packageManager) != null
        if (!isAvailable) {
            val appContext = context.applicationContext
            Toast.makeText(
                appContext,
                appContext.getString(R.string.error_no_camera),
                Toast.LENGTH_LONG
            ).show()
        }
        return isAvailable
    }

}