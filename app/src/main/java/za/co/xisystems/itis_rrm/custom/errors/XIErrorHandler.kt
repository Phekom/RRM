package za.co.xisystems.itis_rrm.custom.errors

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference
import retrofit2.HttpException
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Singleton error handler for RRM
 */

data class XIErrorAction(
    val fragmentReference: WeakReference<Fragment>? = null,
    val view: View,
    val throwable: XIResult.Error,
    val shouldToast: Boolean = false,
    val shouldShowSnackBar: Boolean = false,
    val refreshAction: () -> Unit = {}
)
object XIErrorHandler {
    private const val NO_INTERNET_RESPONSE =
        "Make sure you have an active data / wifi connection"
    private const val NO_CONNECTIVITY_RESPONSE =
        "Network appears to be down, please try again later."
    private const val SERVICE_HOST_UNREACHABLE =
        "Service Host for RRM is down, please try again later."
    private const val EMPTY_RESPONSE = "Server returned empty response."
    const val NO_SUCH_DATA = "Please Check Data Connection \n Or Internet Connection." //\n Or Move to an Area with Good Network Coverage" //Data not found in the database
    const val UNKNOWN_ERROR = "An unknown error occurred!"


    fun handleError(errorAction: XIErrorAction) {
        this.handleError(
            fragment = errorAction.fragmentReference?.get(),
            view = errorAction.view,
            throwable = errorAction.throwable,
            shouldToast = errorAction.shouldToast,
            shouldShowSnackBar = errorAction.shouldShowSnackBar,
            refreshAction = errorAction.refreshAction
        )
    }

    @Suppress("LongParameterList")
    fun handleError(
        fragment: Fragment? = null,
        view: View,
        throwable: XIResult.Error,
        shouldToast: Boolean = false,
        shouldShowSnackBar: Boolean = false,
        refreshAction: () -> Unit = {}
    ) {
        if (shouldShowSnackBar) {
            showSnackBar(view, message = humanReadable(throwable), refresh = refreshAction)
        } else {
            if (shouldToast) {
                // If we don't have a fragment, fallback to dry toast'
                if (fragment != null) {
                    fragment.requireActivity().extensionToast(
                        message = humanReadable(throwable),
                        style = ToastStyle.ERROR,
                        title = null
                    )
                } else {
                    showMessage(view, throwable.message)
                }
            }
        }
        when (throwable.exception) {
            is NoInternetException -> Timber.e(NO_INTERNET_RESPONSE)
            is NoConnectivityException -> Timber.e(NO_CONNECTIVITY_RESPONSE)
            is ServiceHostUnreachableException -> Timber.e(SERVICE_HOST_UNREACHABLE)
            is ServiceException -> Timber.e(throwable.message)
            is HttpException -> Timber.e(
                "HTTP Exception: ${throwable.exception.code()}"
            )
            is NoResponseException -> Timber.e(EMPTY_RESPONSE)
            is NoDataException -> Timber.e(NO_SUCH_DATA)
            else -> Timber.e(throwable.message)
        }
    }

    /**
     * Pop up a snackbar with an option to retry a recoverable operation
     */
    private fun showSnackBar(view: View, message: String, refresh: () -> Unit = {}) {
        IndefiniteSnackbar.show(view, message, refresh)
    }

    private fun showMessage(view: View, message: String) {
        showLongToast(view.context, message)
    }

    private fun showLongToast(context: Context, message: String) = Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()

    fun humanReadable(throwable: XIResult.Error): String {
        return when (throwable.exception) {
            is NoInternetException -> {
                Timber.e(NO_INTERNET_RESPONSE)
                NO_INTERNET_RESPONSE
            }
            is NoConnectivityException -> {
                Timber.e(NO_CONNECTIVITY_RESPONSE)
                NO_CONNECTIVITY_RESPONSE
            }
            is ServiceHostUnreachableException -> {
                Timber.e(SERVICE_HOST_UNREACHABLE)
                SERVICE_HOST_UNREACHABLE
            }
            is HttpException -> {
                Timber.e(
                    "HTTP Exception: ${throwable.exception.code()}"
                )
                "HTTP Exception: ${throwable.exception.code()}"
            }
            is NoResponseException -> {
                Timber.e(EMPTY_RESPONSE)
                EMPTY_RESPONSE
            }
            is NoDataException -> {
                Timber.e(NO_SUCH_DATA)
                NO_SUCH_DATA
            }
            else -> {
                Timber.e(throwable.message)
                throwable.message
            }
        }
    }
}
