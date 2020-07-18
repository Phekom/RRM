package za.co.xisystems.itis_rrm.utils.errors

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import retrofit2.HttpException
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.NoResponseException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.ui.custom.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.utils.results.XIError

//
// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.
//

/**
 * Singleton error handler for RRM
 */
object ErrorHandler {
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
            is ApiException -> Timber.e(throwable.message)
            is HttpException -> Timber.e(
                "HTTP Exception: ${throwable.exception.code()}"
            )
            is NoResponseException -> Timber.e(EMPTY_RESPONSE)
            is NoDataException -> Timber.e(NO_SUCH_DATA)
            else -> Timber.e(throwable.message)
        }
    }

    fun showMessage(view: View, message: String) {
        showLongToast(view.context, message)
    }

    private fun showSnackBar(view: View, message: String, refresh: () -> Unit = {}) {
        IndefiniteSnackbar.show(view, message, refresh)
    }

    private fun showLongToast(context: Context, message: String) = Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}
