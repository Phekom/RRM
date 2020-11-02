package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R

fun Activity.motionToast(message: String, motionType: String) {

    MotionToast.createColorToast(
        context = this,
        message = message,
        style = motionType,
        position = MotionToast.GRAVITY_BOTTOM,
        duration = MotionToast.LONG_DURATION,
        font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
    )
}

fun Fragment.motionToast(message: String, motionType: String = MotionToast.TOAST_INFO, position: Int = MotionToast.GRAVITY_BOTTOM) {
    MotionToast.createColorToast(
        context = this.requireActivity(),
        message = message,
        style = motionType,
        position = position,
        duration = MotionToast.LONG_DURATION,
        font = ResourcesCompat.getFont(this.requireActivity(), R.font.helvetica_regular)
    )
}
