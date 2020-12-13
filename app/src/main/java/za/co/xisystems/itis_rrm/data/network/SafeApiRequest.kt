package za.co.xisystems.itis_rrm.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import za.co.xisystems.itis_rrm.custom.errors.ServiceException

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
open class SafeApiRequest {

    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = withContext(Dispatchers.IO) { call.invoke() }
        return withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                return@withContext response.body()!!
            } else {
                val error = response.errorBody()?.string()

                val message = StringBuilder()
                error?.let {
                    try {
                        message.append(JSONObject(it).getString("message"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    message.append("\n")
                }
                message.append("Error Code: ${response.code()}")
                throw ServiceException(message.toString())
            }
        }
    }
}
