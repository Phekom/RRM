package za.co.xisystems.itis_rrm.ui.scopes

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
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
    private val handler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            Timber.d("uiScope throwing: ${throwable.message}")
            println(throwable)
        }
        Timber.e(throwable)
        throw throwable
    }

    private var job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Job(job).plus(Dispatchers.Main).plus(handler)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onCreate() {
        job = SupervisorJob()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun destroy() = job.cancelChildren()
}
