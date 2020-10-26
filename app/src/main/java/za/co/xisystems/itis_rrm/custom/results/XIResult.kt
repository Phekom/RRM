package za.co.xisystems.itis_rrm.custom.results

import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import java.io.IOException
import java.net.SocketException

// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.

/**
 * Result - contains either data or an error
 * @param T : Any
 */

sealed class XIResult<out T : Any>

class XISuccess<out T : Any>(val data: T) : XIResult<T>()

class XIError(
    val exception: Throwable,
    val message: String = exception.message ?: XIErrorHandler.UNKNOWN_ERROR
) : XIResult<Nothing>()

class XIProgress(val isLoading: Boolean) : XIResult<Nothing>()

class XIStatus(val message: String) : XIResult<Nothing>()

fun XIError.isConnectivityException(): Boolean {
    return when (exception) {
        is IOException -> true
        is NoInternetException -> true
        is NoConnectivityException -> true
        is ServiceHostUnreachableException -> true
        is SocketException -> true
        else -> false
    }
}
