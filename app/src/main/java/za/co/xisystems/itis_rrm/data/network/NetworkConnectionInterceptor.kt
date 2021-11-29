package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.utils.ServiceUriUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {
    private val testConnection = "8.8.8.8"
    private val serviceHost = BuildConfig.API_HOST//ServiceUriUtil.getInstance()?.webServiceHost
    private val applicationContext = context.applicationContext
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!ServiceUtil.isNetworkAvailable(applicationContext)) {
            throw NoInternetException("Please ensure you have an active data connection")
        }

//        if (isHostAvailable(testConnection)) {
//            throw NoConnectivityException(
//                "Network appears to be down, please try again later. Host: $testConnection"
//            )
//        }

        if (isHostAvailable(serviceHost!!)) {
            throw ServiceHostUnreachableException(
                "Service Host for RRM is down, please try again later. Host: $serviceHost"
            )
        }

        return chain.proceed(chain.request())
}

fun isHostAvailable(testURL: String): Boolean {
//    , port = 443, timeout = 60_000L
    return ping(url = URL(testURL))
}

fun ping(url: URL, timeout_millis: Int = 50000): Boolean {
    val connection = url.openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = timeout_millis
        connection.readTimeout = timeout_millis
        connection.requestMethod = "HEAD"
        val responseCode = connection.responseCode
        connection.disconnect()
        HttpURLConnection.HTTP_OK <= responseCode && responseCode < HttpURLConnection.HTTP_BAD_REQUEST
    } catch (exception: IOException) {
        false
    } finally {
        connection.disconnect()
    }


}

}