package za.co.xisystems.itis_rrm.custom.results

import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.data.network.responses.ErrorResponse

// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.

/**
 * Result - contains either data or an error
 * @param T : Any
 */

sealed class XIResult<out T : Any> {
    companion object {
        fun progress(isLoading: Boolean): XIResult<Nothing> = Progress(isLoading)
        fun success(data: Any): XIResult<Any> = Success(data)
        fun error(exception: Throwable, message: String): XIResult<Nothing> = Error(exception, message)
        fun status(message: String): XIResult<Nothing> = Status(message)
        fun progressUpdate(key: String, ratio: Float): XIResult<Nothing> = ProgressUpdate(key, ratio)
        fun webException(code: Int, error: ErrorResponse): XIResult<Nothing> = RestException(code, error)
    }

    class Success<out T : Any>(val data: T) : XIResult<T>()

    class Error(
        val exception: Throwable,
        val message: String
    ) : XIResult<Nothing>()

    class RestException(
        val code: Int? = null,
        val error: ErrorResponse? = null
    ) : XIResult<Nothing>()

    class ProgressUpdate(val key: String, val ratio: Float = 0.0f) : XIResult<Nothing>()

    class Progress(val isLoading: Boolean) : XIResult<Nothing>()

    class Status(val message: String) : XIResult<Nothing>()
}

/**
 * A catch-all implementation for network errors, since they can be common,
 * but are usually transient - the user can retry the operation.
 */
fun XIResult.Error.isRecoverableException(): Boolean {
    return when (exception) {
        is IOException -> true
        is NoInternetException -> true
        is NoConnectivityException -> true
        is ServiceHostUnreachableException -> true
        is SocketException -> true
        is SocketTimeoutException -> true
        else -> false
    }
}

const val PERCENT_RATIO: Float = 100.0f

fun XIResult.ProgressUpdate.getPercentageComplete(): Float {
    return this.ratio * PERCENT_RATIO
}
