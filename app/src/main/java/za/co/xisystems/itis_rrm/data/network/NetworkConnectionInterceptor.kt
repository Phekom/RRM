package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.Interceptor
import okhttp3.Response
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.Constants
import za.co.xisystems.itis_rrm.utils.NoConnectivityException
import za.co.xisystems.itis_rrm.utils.NoInternetException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {
//    private val testConnection = "https://itisqa.nra.co.za/ITISServicesMobile"
    private val applicationContext = context.applicationContext

    @RequiresApi(Build.VERSION_CODES.M)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable())
            throw NoInternetException("Make sure you have an active data connection")

//        if (!isHostReachable())
//            throw NoConnectivityException("Server unreachable, make sure you have data loaded.")

        return chain.proceed(chain.request())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }
        return result
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun isHostReachable(): Boolean {
//        var result: Boolean
//        try {
//            val connection = URL(testConnection).openConnection() as HttpURLConnection
//            connection.setRequestProperty("User-Agent", "Test")
//            connection.setRequestProperty("Connection", "close")
//            connection.connectTimeout = 1000
//            connection.connect()
//            result = (connection.responseCode == 200)
//        } catch (e: IOException) {
//            result = false
//        }
//
//        return result
//    }

}
