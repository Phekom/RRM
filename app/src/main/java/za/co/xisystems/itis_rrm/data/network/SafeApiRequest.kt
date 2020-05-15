package za.co.xisystems.itis_rrm.data.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.ApiException

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
abstract class SafeApiRequest {

    open suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody()?.string()

            val message = StringBuilder()
            try {
                error?.let {
                    message.append("Error Code: ${response.code()}")
                    message.append("\t")
                    message.append(JSONObject(it).getString("message"))
                }


            } catch (e: JSONException) {
                Timber.e(e, e.localizedMessage)
            }

            throw ApiException(message.toString())

        }
    }

}

