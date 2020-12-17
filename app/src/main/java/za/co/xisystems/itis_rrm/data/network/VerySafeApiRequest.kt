package za.co.xisystems.itis_rrm.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import za.co.xisystems.itis_rrm.custom.errors.ConnectException
import za.co.xisystems.itis_rrm.custom.errors.NoResponseException
import za.co.xisystems.itis_rrm.custom.errors.RestException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
open class VerySafeApiRequest {

    private lateinit var apiException: Throwable

    suspend fun <T : Any> wrappedRequest(call: suspend () -> Response<T>): T? {
        try {
            val response = withContext(Dispatchers.IO) { call.invoke() }
            if (response.body() != null && response.isSuccessful) {
                return response.body()!!
            } else {
                throw NoResponseException("Server returned empty response")
            }
        } catch (throwable: Throwable) {
            withContext(Dispatchers.Main) {
                apiException = when (throwable) {
                    is IOException -> ConnectException(throwable.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    is HttpException -> {
                        val message = "Service call failed with status: ${throwable.code()}"
                        RestException(message, throwable)
                    }
                    else -> throwable
                }
                throw apiException
            }
        }
    }
}
