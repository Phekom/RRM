package za.co.xisystems.itis_rrm.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler

/**
 * Created by Francis Mahlava on 2019/10/20.
 */

val uncaughtExceptionHandler = CoroutineExceptionHandler { _, exception ->
    when (exception) {
        is CancellationException -> {
            Timber.i(exception, exception.message ?: XIErrorHandler.UNKNOWN_ERROR)
        }
        else -> {
            Timber.e(exception, exception.message ?: XIErrorHandler.UNKNOWN_ERROR)
            if (BuildConfig.DEBUG) {
                println("$exception caught!")
                throw exception
            }
        }
    }
}

/**
 *
 * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, T>
 * @return Lazy<Deferred<T>>
 */
fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
    return lazy {
        CoroutineScope(context = Dispatchers.Default + uncaughtExceptionHandler).async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}
