package za.co.xisystems.itis_rrm.ui.extensions

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.isRecoverableException

fun Fragment.crashGuard(
    throwable: XIResult.Error,
    refreshAction: (() -> Unit)? = null
) {

    when (throwable.isRecoverableException() && refreshAction != null) {

        true -> {
            this.extensionToast(
                title = "Network Action Failed",
                message = throwable.exception.message ?: XIErrorHandler.UNKNOWN_ERROR,
                style = ToastStyle.NO_INTERNET,
                position = ToastGravity.BOTTOM,
                duration = ToastDuration.LONG
            )

            Handler(Looper.getMainLooper()).postDelayed(
                {
                    XIErrorHandler.handleError(
                        fragment = this,
                        view = this.requireView(),
                        throwable = throwable,
                        shouldShowSnackBar = true,
                        refreshAction = { refreshAction() }
                    )
                },
                Constants.TWO_SECONDS
            )
        }
        else -> {
            this.extensionToast(
                message = XIErrorHandler.humanReadable(throwable),
                style = ToastStyle.ERROR,
                position = ToastGravity.BOTTOM,
                duration = ToastDuration.LONG
            )
        }
    }
}
