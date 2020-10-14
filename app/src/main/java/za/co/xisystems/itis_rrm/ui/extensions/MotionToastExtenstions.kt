package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
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

fun FragmentActivity.motionToast(message: String, motionType: String) {
    MotionToast.createColorToast(
        context = this,
        message = message,
        style = motionType,
        position = MotionToast.GRAVITY_BOTTOM,
        duration = MotionToast.LONG_DURATION,
        font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
    )
}
