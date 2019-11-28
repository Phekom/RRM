package za.co.xisystems.itis_rrm.data.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import za.co.xisystems.itis_rrm.utils.ApiException
/**
 * Created by Francis Mahlava on 2019/10/18.
 */
abstract class SafeApiRequest {

    suspend fun<T: Any> apiRequest(call: suspend () -> Response<T>) : T{
        val response = call.invoke()
        if(response.isSuccessful){
            return response.body()!!
        }else{
            val error = response.errorBody()?.string()

            val message = StringBuilder()
            error?.let{
                try{
                    message.append(JSONObject(it).getString("message"))
                }catch(e: JSONException){ }
                message.append("\n")
            }
            message.append("Error Code: ${response.code()}")
            throw ApiException(message.toString())
        }
    }

}