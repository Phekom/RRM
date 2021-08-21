package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle

fun Activity.extensionToast(
    title: String? = null,
    message: String,
    style: ToastStyle = ToastStyle.INFO,
    position: ToastGravity = ToastGravity.BOTTOM,
    duration: ToastDuration = ToastDuration.LONG
) {

    MotionToast.createColorToast(
        context = this,
        title = title,
        message = message,
        style = style.getValue(),
        position = position.getValue(),
        duration = duration.getValue(),
        font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
    )
}

fun Fragment.extensionToast(
    message: String,
    style: ToastStyle = ToastStyle.INFO,
    position: ToastGravity = ToastGravity.BOTTOM,
    title: String?,
    duration: ToastDuration = ToastDuration.LONG
) {
    MotionToast.createColorToast(
        title = title,
        context = this.requireActivity(),
        message = message,
        style = style.getValue(),
        position = position.getValue(),
        duration = duration.getValue(),
        font = ResourcesCompat.getFont(this.requireContext(), R.font.helvetica_regular)
    )
}
