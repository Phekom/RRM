package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.MutableLiveData
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.custom.errors.ConnectException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.custom.errors.TransmissionException
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.utils.ServiceUriUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
 
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {
    private val serviceUriUtil = ServiceUriUtil.getInstance()!!
    private val serviceHost = BuildConfig.API_HOST//serviceUriUtil.webServiceHost!!
    private val applicationContext = context.applicationContext
    var healthState: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()


    override fun intercept(chain: Interceptor.Chain): Response {
        healthState = MutableLiveData()
        if (!isInternetAvailable()) {
            throw NoInternetException("Make sure you have an active data connection")
        }

        if (isHostAvailable(serviceHost)) {
            throw ServiceHostUnreachableException("Service Host for RRM is down, please try again later. Host: $serviceHost" )
        }else{
            isURLActive(URL(serviceHost), healthState)
        }

//        if (!isURLActive(URL(serviceHost))) {
//            throw ServiceHostUnreachableException(
//                "Service Host for RRM is down, please try again later."
//            )
//        }

        return chain.proceed(chain.request())
    }

    private fun isHostAvailable(testURL: String): Boolean {
        return ping(url = URL(testURL))
    }

    private fun ping(url: URL, timeout_millis: Int = 50000): Boolean {
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

    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                    else -> false
                }
            }
        }
        return result
    }

    private fun isURLActive(url: URL, healthState: MutableLiveData<XIEvent<XIResult<String>>>,timeoutMillis: Int = 60000): Boolean {
        val connection = url.openConnection() as HttpsURLConnection
        return try {
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.requestMethod = "HEAD"
            connection.connect()
            val responseCode = connection.responseCode
            HttpsURLConnection.HTTP_OK <= responseCode && responseCode < HttpsURLConnection.HTTP_BAD_REQUEST
        } catch (exception: IOException) {
//            healthState.value =
//                XIEvent(
//                    XIResult.Error(
//                        TransmissionException(
//                            "Server Communication Error", ConnectException("You Have No Active Data")
//                        ), "You May have Ran out of Data Please Check and Retry.","You Have No Active Data Connection"
//                    )
//                )
            throw ServiceHostUnreachableException("Service Host for RRM is down, please try again later. Host: $serviceHost")
            Timber.e("Could not connect to service host: ${exception.message}")
            false
        } finally {
            connection.disconnect()
        }
    }
}
