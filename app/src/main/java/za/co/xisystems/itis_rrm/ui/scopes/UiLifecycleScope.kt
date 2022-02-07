package za.co.xisystems.itis_rrm.ui.scopes

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import kotlin.coroutines.CoroutineContext

/**
 * Created by Shaun McDonald on 2020/04/06.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Coroutine context that automatically is cancelled when UI is destroyed
 * @property job Job?
 * @property coroutineContext CoroutineContext
 */
class UiLifecycleScope : CoroutineScope, DefaultLifecycleObserver {
    private val handler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            Timber.d("uiScope throwing: ${throwable.message}")
            println(throwable)
        }
        // Cancellation exceptions are par for the course when leaving the scope
        when (throwable) {
            is CancellationException -> {
                // No-op
            }
            else -> {
                Timber.e(throwable)
                // throw throwable
            }
        }
    }

    private var superJob = SupervisorJob()
    private var job = Job(superJob)

    override val coroutineContext: CoroutineContext
        get() = job.plus(Dispatchers.Main).plus(handler)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        superJob = SupervisorJob()
        job = Job(superJob)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        superJob.cancelChildren()
    }
}
