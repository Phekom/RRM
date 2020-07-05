package za.co.xisystems.itis_rrm.utils

import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig

/**
 * Created by Francis Mahlava on 2019/10/20.
 */

val uncaughtExceptionHandler = CoroutineExceptionHandler { _, exception ->

    if (BuildConfig.DEBUG) {
        println("$exception caught!")
    }

    Timber.e(exception, exception.localizedMessage)
    throw exception
}

/**
 *
 * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, T>
 * @return Lazy<Deferred<T>>
 */
fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>> {
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY, context = uncaughtExceptionHandler) {
            block.invoke(this)
        }
    }
}
