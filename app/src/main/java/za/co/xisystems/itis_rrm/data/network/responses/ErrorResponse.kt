package za.co.xisystems.itis_rrm.data.network.responses

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Error response class from broken API call
 */
data class ErrorResponse(val code: Int, val message: String)