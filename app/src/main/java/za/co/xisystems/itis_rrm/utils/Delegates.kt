package za.co.xisystems.itis_rrm.utils

import kotlinx.coroutines.*

/**
 * Created by Francis Mahlava on 2019/10/20.
 */

fun<T> lazyDeferred(block: suspend CoroutineScope.() -> T): Lazy<Deferred<T>>{
    return lazy {
        GlobalScope.async(start = CoroutineStart.LAZY) {
            block.invoke(this)
        }
    }
}