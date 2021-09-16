package za.co.xisystems.itis_rrm.forge

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig

class ArmouryScope(
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : CoroutineScope, LifecycleObserver {

    private val handler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            println(throwable)
        }
        Timber.e(throwable)
        throw ArmouryException(
            message = "XIArmoury encountered a problem: ${throwable.message ?: "Unknown Error"}",
            throwable
        )
    }

    private lateinit var supervisorJob: Job

    override val coroutineContext: CoroutineContext
        get() = Job(supervisorJob).plus(dispatchers.default()).plus(handler)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onCreate() {
        supervisorJob = SupervisorJob()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun destroy() {
        supervisorJob.cancelChildren()
    }
}
