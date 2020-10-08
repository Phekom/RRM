package za.co.xisystems.itis_rrm.custom.errors

import android.content.Context
import android.view.View
import android.widget.Toast
import retrofit2.HttpException
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Singleton error handler for RRM
 */
object XIErrorHandler {
    private const val NO_INTERNET_RESPONSE =
        "Make sure you have an active data / wifi connection"
    private const val NO_CONNECTIVITY_RESPONSE =
        "Network appears to be down, please try again later."
    private const val SERVICE_HOST_UNREACHABLE =
        "Service Host for RRM is down, please try again later."
    private const val EMPTY_RESPONSE = "Server returned empty response."
    const val NO_SUCH_DATA = "Data not found in the database"
    const val UNKNOWN_ERROR = "An unknown error occurred!"

    fun handleError(
        view: View,
        throwable: XIError,
        shouldToast: Boolean = false,
        shouldShowSnackBar: Boolean = false,
        refreshAction: () -> Unit = {}
    ) {
        if (shouldShowSnackBar) {
            showSnackBar(view, message = throwable.message, refresh = refreshAction)
        } else {
            if (shouldToast) {
                showLongToast(view.context, throwable.message)
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

    private fun showSnackBar(view: View, message: String, refresh: () -> Unit = {}) {
        IndefiniteSnackbar.show(view, message, refresh)
    }

    fun showMessage(view: View, message: String) {
        showLongToast(view.context, message)
    }

    private fun showLongToast(context: Context, message: String) = Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}
