package za.co.xisystems.itis_rrm.data.network

import com.google.gson.Gson
import java.io.IOException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import za.co.xisystems.itis_rrm.custom.errors.RecoverableException
import za.co.xisystems.itis_rrm.custom.errors.RestException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.data.network.responses.ErrorResponse
import za.co.xisystems.itis_rrm.utils.DispatcherProvider

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
open class SafeApiRequest(
    private val dispatchers: za.co.xisystems.itis_rrm.utils.DispatcherProvider = za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider()
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T = withContext(dispatchers.io()) {
        try {
            val response = call.invoke()
            return@withContext response.body()!!
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> {
                    val message = "Network connectivity issue: ${
                    throwable.message ?: XIErrorHandler.UNKNOWN_ERROR
                    }"
                    throw RecoverableException(message)
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)

                    val message = "$code - ${errorResponse?.message}"
                    throw RestException(message, throwable)
                }
                else -> {
                    val message = "Unclassified Error: ${
                    throwable.message ?: XIErrorHandler.UNKNOWN_ERROR
                    }"
                    throw ServiceException(message)
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            throwable.response()?.errorBody()?.source()?.let {
                val gson = Gson()
                gson.fromJson(it.toString(), ErrorResponse::class.java)
            }
        } catch (exception: Exception) {
            null
        }
    }
}
