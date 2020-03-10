package za.co.xisystems.itis_rrm.utils

import android.util.Log
import kotlinx.coroutines.*

/**
 * Created by Francis Mahlava on 2019/10/20.
 */

val uncaughtExceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("$exception caught!")
    when (exception) {
        is NoConnectivityException -> throw exception
        is NoInternetException -> throw exception
        else -> {
            Log.e("LazyDeferred", "UncaughtException", exception)
            exception.printStackTrace()
            throw exception
        }
    }
}

fun<T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>>{
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY, context = uncaughtExceptionHandler) {
            block.invoke(this)
        }
    }
}
