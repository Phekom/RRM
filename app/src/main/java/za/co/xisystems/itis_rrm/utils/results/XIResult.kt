package za.co.xisystems.itis_rrm.utils.results

import za.co.xisystems.itis_rrm.utils.errors.ErrorHandler


// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.

/**
 * Result - contains either data or an error
 * @param out T : Any
 */

sealed class XIResult<out T : Any>

class XISuccess<out T : Any>(val data: T) : XIResult<T>()

class XIError(
    val exception: Throwable,
    val message: String = exception.message ?: ErrorHandler.UNKNOWN_ERROR
) : XIResult<Nothing>()


class XIProgress(val isLoading: Boolean) : XIResult<Nothing>()