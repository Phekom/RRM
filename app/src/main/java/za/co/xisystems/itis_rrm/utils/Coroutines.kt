package za.co.xisystems.itis_rrm.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Francis Mahlava on 2019/10/18.
 */
object Coroutines {

    val connectivityHandler = CoroutineExceptionHandler { _, exception ->
        when (exception) {
            is NoInternetException -> throw exception
            is NoConnectivityException -> throw exception
            else -> {
                Timber.e(exception, exception.message)
                exception.printStackTrace()
                throw exception
            }
        }
    }

    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO + connectivityHandler).launch {
            work()
        }

    fun api(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO + connectivityHandler).launch {
            work()
        }






}