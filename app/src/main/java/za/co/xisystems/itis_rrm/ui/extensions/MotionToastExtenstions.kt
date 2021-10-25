package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import java.lang.ref.WeakReference

fun Activity.extensionToast(
    title: String? = null,
    message: String,
    style: MotionToastStyle = MotionToastStyle.INFO,
    position: ToastGravity = ToastGravity.BOTTOM,
    duration: ToastDuration = ToastDuration.LONG
) {

    MotionToast.createColorToast(
        context = WeakReference(this).get()!!,
        title = title,
        message = message,
        style = style,
        position = position.getValue(),
        duration = duration.getValue(),
        font = ResourcesCompat.getFont(WeakReference(this).get()!!, R.font.helvetica_regular)
    )
}

fun Fragment.extensionToast(
    colorToast: ColorToast
) {
    MotionToast.createColorToast(
        title = colorToast.title,
        context = WeakReference(this.requireActivity()).get()!!,
        message = colorToast.message,
        style = colorToast.style,
        position = colorToast.gravity.getValue(),
        duration = colorToast.duration.getValue(),
        font = ResourcesCompat.getFont(WeakReference(this.requireActivity()).get()!!, R.font.helvetica_regular)
    )
}

fun Fragment.extensionToast(
    message: String,
    style: MotionToastStyle = MotionToastStyle.INFO,
    position: ToastGravity = ToastGravity.BOTTOM,
    title: String? = null,
    duration: ToastDuration = ToastDuration.LONG
) {
    val newToast = ColorToast(
        title,
        message,
        style,
        position,
        duration
    )
    this.extensionToast(newToast)
}
