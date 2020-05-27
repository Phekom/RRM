package za.co.xisystems.itis_rrm.utils.results

import za.co.xisystems.itis_rrm.utils.errors.ErrorHandler


// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.

/**
 * Result - contains either data or an error
 * @param out T : Any
 */

sealed class ResultSet<out T : Any>

class Success<out T : Any>(val data: T) : ResultSet<T>()

class Failure(
    val exception: Throwable,
    val message: String = exception.message ?: ErrorHandler.UNKNOWN_ERROR
) : ResultSet<Nothing>()

class Progress(val isLoading: Boolean) : ResultSet<Nothing>()