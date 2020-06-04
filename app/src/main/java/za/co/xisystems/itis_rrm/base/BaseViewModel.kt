package za.co.xisystems.itis_rrm.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

/**
 * Created by Shaun McDonald on 2020/05/29.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/

/**
 * BaseViewModel
 * @property scope CoroutineScope
 * The scope property is so that we can instantly have coroutines to launch inside
 * or view models without all the boiler plate.
 */

abstract class BaseViewModel : ViewModel() {

    protected val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + uncaughtExceptionHandler
    )

    override fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }

}