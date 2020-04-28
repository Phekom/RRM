package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {
    private val testConnection = "www.google.com"
    private val serviceURL = "itisqa.nra.co.za"
    private val applicationContext = context.applicationContext


    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable())
            throw NoInternetException("Please ensure you have an active data connection")

        if (!isHostAvailable(host = testConnection, port = 443, timeout = 1000))
            throw NoConnectivityException(
                "Network appears to be down, please try again later."
            )

        if (!isHostAvailable(host = serviceURL, port = 443, timeout = 1000)) {
            throw ServiceHostUnreachableException(
                "Service Host for RRM is down, please try again later."
            )
        }

        return chain.proceed(chain.request())
    }


    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager?.let {
                it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        else -> false
                    }
                }
            }
        } else {
            // For Android versions older than Marshmallow, this will work.
            connectivityManager?.run {
                @Suppress("DEPRECATION")
                activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        else -> false
                    }
                }
            }
        }

        return result
    }

    fun isHostAvailable(host: String?, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                val inetAddress: InetAddress = InetAddress.getByName(host)
                val inetSocketAddress = InetSocketAddress(inetAddress, port)
                socket.connect(inetSocketAddress, timeout)
                socket.close()
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

}
