package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.utils.ServiceUriUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {
    private val testConnection = "www.mg.co.za"
    private val serviceHost = ServiceUriUtil.getInstance()?.webServiceHost
    private val applicationContext = context.applicationContext
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!ServiceUtil.isNetworkAvailable(applicationContext)) {
            throw NoInternetException("Please ensure you have an active data connection")
        }

        if (!ServiceUtil.isHostAvailable(host = testConnection, port = 443, timeout = 50000)) {
            throw NoConnectivityException(
                "Network appears to be down, please try again later. Host: $testConnection"
            )
        }

        if (!ServiceUtil.isHostAvailable(host = serviceHost, port = 80, timeout = 50000)) {
            throw ServiceHostUnreachableException(
                "Service Host for RRM is down, please try again later. Host: $serviceHost"
            )
        }

        return chain.proceed(chain.request())
    }
}
