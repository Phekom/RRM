package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Activity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import java.lang.ref.WeakReference

fun Activity.extensionToast(
    title: String? = null,
    message: String,
    style: ToastStyle = ToastStyle.INFO,
    position: ToastGravity = ToastGravity.BOTTOM,
    duration: ToastDuration = ToastDuration.LONG
) {
    var mWeakRef: WeakReference<Activity>? = WeakReference(this)
    val weakActivity = mWeakRef?.get()!!
    MotionToast.createColorToast(
        context = weakActivity,
        title = title,
        message = message,
        style = style.getValue(),
        position = position.getValue(),
        duration = duration.getValue(),
        font = ResourcesCompat.getFont(weakActivity, R.font.helvetica_regular)
    ).also {
        mWeakRef = null
    }
}

fun Fragment.extensionToast(
    colorToast: ColorToast
) {
    var mWeakRef: WeakReference<Activity>? = WeakReference(this.requireActivity())
    val weakActivity = mWeakRef?.get()!!
    MotionToast.createColorToast(
        title = colorToast.title,
        context = weakActivity,
        message = colorToast.message,
        style = colorToast.style.getValue(),
        position = colorToast.gravity.getValue(),
        duration = colorToast.duration.getValue(),
        font = ResourcesCompat.getFont(weakActivity, R.font.helvetica_regular)
    ).also {
        mWeakRef = null
    }
}

fun Fragment.extensionToast(
    message: String,
    style: ToastStyle = ToastStyle.INFO,
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
