package za.co.xisystems.itis_rrm.ui.scopes

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig

/**
 * Created by Shaun McDonald on 2020/04/06.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Coroutine context that automatically is cancelled when UI is destroyed
 * @property job Job?
 * @property coroutineContext CoroutineContext
 */
class UiLifecycleScope : CoroutineScope, LifecycleObserver {
    val handler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            println(throwable)
        }
        Timber.e(throwable)
        throw Exception(throwable)
    }

    private var job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = job.plus(Dispatchers.Main).plus(handler)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onCreate() {
        job = SupervisorJob()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun destroy() = coroutineContext.cancelChildren()
}
