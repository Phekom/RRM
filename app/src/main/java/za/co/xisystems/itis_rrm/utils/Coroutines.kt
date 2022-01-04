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

    private val connectivityHandler = CoroutineExceptionHandler { _, exception ->

        Timber.e(Exception(exception))
        Exception(exception).printStackTrace()
        throw Exception(exception)
    }

    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun api(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun default(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Default).launch {
            work()
        }

    fun ui(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main.immediate).launch {
            work()
        }
}
