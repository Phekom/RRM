package za.co.xisystems.itis_rrm.base

import za.co.xisystems.itis_rrm.custom.errors.ErrorHandler

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * A class to contain data or errors resulting from an action or query
 */
sealed class Result<out T : Any>

class Success<out T : Any>(val data: T) : Result<T>()

class Error(
    val exception: Throwable,
    val message: String = exception.message ?: ErrorHandler.UNKNOWN_ERROR
) : Result<Nothing>()

class Progress(val isLoading: Boolean) : Result<Nothing>()
