package za.co.xisystems.itis_rrm.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class CoroutineConnectivityExceptionHandler(override val key: CoroutineContext.Key<*>) : CoroutineExceptionHandler {

    override fun handleException(
        context: CoroutineContext,
        exception: Throwable
    ) {
        when(exception){

        }
    }


}