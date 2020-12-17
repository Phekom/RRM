package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R

fun Activity.extensionToast(message: String, motionType: String) {

    MotionToast.createColorToast(
        context = this,
        message = message,
        style = motionType,
        position = MotionToast.GRAVITY_BOTTOM,
        duration = MotionToast.LONG_DURATION,
        font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
    )
}

fun Fragment.extensionToast(
    message: String,
    motionType: String = MotionToast.TOAST_INFO,
    position: Int = MotionToast.GRAVITY_BOTTOM,
    title: String?
) {
    if (title.isNullOrBlank()) {
        MotionToast.createColorToast(
            context = this.requireActivity(),
            message = message,
            style = motionType,
            position = position,
            duration = MotionToast.LONG_DURATION,
            font = ResourcesCompat.getFont(this.requireContext(), R.font.helvetica_regular)
        )
    } else {
        MotionToast.createColorToast(
            context = this.requireActivity(),
            title = title,
            message = message,
            style = motionType,
            position = position,
            duration = MotionToast.LONG_DURATION,
            font = ResourcesCompat.getFont(this.requireContext(), R.font.helvetica_regular)
        )
    }
}
