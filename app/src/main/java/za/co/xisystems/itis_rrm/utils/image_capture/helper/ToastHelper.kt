package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
class ToastHelper {
    companion object {
        var toast: Toast? = null

        @SuppressLint("ShowToast")
        fun show(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
            if (toast == null) {
                toast = Toast.makeText(context.applicationContext, text, duration)
            } else {
                toast?.cancel()
                toast?.setText(text)
            }
            toast?.show()
        }
    }
}