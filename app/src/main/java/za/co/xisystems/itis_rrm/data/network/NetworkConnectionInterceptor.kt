package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceHostUnreachableException
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.utils.ServiceUriUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
class NetworkConnectionInterceptor(
    private var context: Context
) : Interceptor {
    private val testConnection = "www.nra.co.za"
    private val serviceHost = ServiceUriUtil.getInstance()?.webServiceHost
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!context.isConnected) {
            throw NoInternetException("Please ensure you have an active data connection")
        }

        if (!ServiceUtil.isHostAvailable(host = testConnection, port = 443, timeout = 5000)) {
            throw NoConnectivityException(
                "Network appears to be down, please try again later."
            )
        }

        if (!ServiceUtil.isHostAvailable(host = serviceHost, port = 443, timeout = 5000)) {
            throw ServiceHostUnreachableException(
                "Service Host for RRM is down, please try again later."
            )
        }

        return chain.proceed(chain.request())
    }
}
