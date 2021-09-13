/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

object ServiceUtil {
    fun isNetworkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

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

    fun isHostAvailable(host: String?, port: Int, timeout: Long): Boolean {
        val socket = Socket()
        var result = false
        // Anti exception-swallowing implementation
        try {
            val inetAddress: InetAddress = InetAddress.getByName(host)
            val inetSocketAddress = InetSocketAddress(inetAddress, port)
            socket.connect(inetSocketAddress, timeout.toInt())
            socket.close()
            result = true
        } catch (e: Exception) {
            Timber.e("Could not connect to $host::$port: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}")
        } finally {
            socket.close()
        }
        return result
    }

    fun isNetworkConnected(applicationContext: Context): Boolean {
        return isNetworkAvailable(applicationContext)
    }
}
