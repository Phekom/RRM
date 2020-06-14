package za.co.xisystems.itis_rrm.utils.errors

import android.content.Context
import android.view.View
import android.widget.Toast
import retrofit2.HttpException
import timber.log.Timber
import za.co.xisystems.itis_rrm.ui.custom.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.utils.NoDataException
import za.co.xisystems.itis_rrm.utils.NoResponseException
import za.co.xisystems.itis_rrm.utils.results.XIError
import java.io.IOException

//
// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
object ErrorHandler {

    private const val NETWORK_ERROR_MESSAGE =
        "Please check your internet connectivity and try again!"
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
            is IOException -> Timber.e(NETWORK_ERROR_MESSAGE)
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

    private fun showLongToast(context: Context, message: String) = Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}
