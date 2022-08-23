package za.co.xisystems.itis_rrm.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import com.github.ajalt.timberkt.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress


/**
 * Created by Francis.Mahlava on 2022/05/11.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

const val TAG = "ConnectionManager"

class ConnectionLiveData(context: Context) : LiveData<Boolean>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        @SuppressLint("TimberArgCount")
        override fun onAvailable(network: Network) {
            Timber.tag(TAG).d("onAvailable: %s", network)
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            Timber.tag(TAG).d( "onAvailable: %s", network, hasInternetCapability)

            if (hasInternetCapability == true) {
                // Check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                    if (hasInternet) {
                        withContext(Dispatchers.Main) {
                            Timber.tag(TAG).d("onAvailable: adding network. %s", network)
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }else{
                        withContext(Dispatchers.Main) {
                            Timber.tag(TAG).d("onAvailable: adding network. %s", network)
                            validNetworks.add(network)
                            checkValidNetworks()
                        }
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            Timber.d() { "onLost: " + network }
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }

    object DoesNetworkHaveInternet {

        fun execute(socketFactory: javax.net.SocketFactory): Boolean {
            // Make sure to execute this on a background thread.
            return try {
                Timber.d(){"PINGING Google..."}
                val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
                socket.connect(InetSocketAddress("8.8.4.4", 53), 1500)
                socket.close()
                Timber.d(null) { "PING success." }
                true
            } catch (e: IOException) {
                Timber.e(e) { "No Internet Connection. " }
                false
            }
        }
    }
}

