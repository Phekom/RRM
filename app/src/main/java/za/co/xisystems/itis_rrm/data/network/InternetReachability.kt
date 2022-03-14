package za.co.xisystems.itis_rrm.data.network

import android.content.Context
import android.net.ConnectivityManager
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object InternetReachability {
    val LANDING_SERVER = "https://itis.nra.co.za/MobileServices/api/RRM/"
    val REACHABILITY_SERVER = "https://www.google.com"

    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        Timber.d("hasNetworkAvailable: ${(network != null)}")

        return (network?.isConnected) ?: false
    }

//    fun hasInternetConnected(context: Context): Boolean {
//        if (hasNetworkAvailable(context)) {
//            try {
//                val connection = URL(REACHABILITY_SERVER).openConnection() as HttpURLConnection
//                connection.setRequestProperty("User-Agent", "Test")
//                connection.setRequestProperty("Connection", "close")
//                connection.connectTimeout = 1500 // configurable
//                connection.connect()
//                Timber.d( "hasInternetConnected: ${(connection.responseCode == 200)}")
//                return (connection.responseCode == 200)
//            } catch (e: IOException) {
//                Timber.d( e,"Error checking internet connection")
//            }
//        } else {
//            Timber.d( "No network available!")
//        }
//        Timber.d( "hasInternetConnected: false")
//        return false
//    }

    fun hasServerConnected(context: Context): Boolean {
        var connectionActive = false
        if (hasNetworkAvailable(context)) {
            try {
                val connection = URL(LANDING_SERVER).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Test")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1500 // configurable
                connection.connect()
                Timber.d( "hasServerConnected: ${(connection.responseCode == 200)}")
                connectionActive = true
                return (connection.responseCode == 200)
            } catch (e: IOException) {
                Timber.d(e, "Error checking internet connection")
            }
        } else {
            Timber.d("Server is unavailable!")
        }
        Timber.d("hasServerConnected: false")
        return connectionActive
    }
}