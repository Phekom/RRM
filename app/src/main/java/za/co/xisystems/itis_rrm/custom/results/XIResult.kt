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
        fun progress(isLoading: Boolean): XIResult<Nothing> = XIProgress(isLoading)
        fun success(data: Any): XIResult<Any> = XISuccess(data)
        fun error(exception: Throwable, message: String): XIResult<Nothing> = XIError(exception, message)
        fun status(message: String): XIResult<Nothing> = XIStatus(message)
        fun progressUpdate(key: String, ratio: Float): XIResult<Nothing> = XIProgressUpdate(key, ratio)
        fun webException(code: Int, error: ErrorResponse): XIResult<Nothing> = XIRestException(code, error)
    }
}

class XISuccess<out T : Any>(val data: T) : XIResult<T>()

class XIError(
    val exception: Throwable,
    val message: String
) : XIResult<Nothing>()

class XIRestException(
    val code: Int? = null,
    val error: ErrorResponse? = null
) : XIResult<Nothing>()

class XIProgressUpdate(val key: String, val ratio: Float = 0.0f) : XIResult<Nothing>()

class XIProgress(val isLoading: Boolean) : XIResult<Nothing>()

class XIStatus(val message: String) : XIResult<Nothing>()

/**
 * A catch-all implementation for network errors, since they can be common,
 * but are usually transient - the user can retry the operation.
 */
fun XIError.isRecoverableException(): Boolean {
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

fun XIProgressUpdate.getPercentageComplete(): Float {
    return this.ratio * PERCENT_RATIO
}
