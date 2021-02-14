/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/15 12:29 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

/**
 * Created by Shaun McDonald on 2020/05/29.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/

/**
 * BaseVieModel uses ViewModelScope
 * @property superJob CompletableJob - one job to close for onCleared()
 * @property ioContext CoroutineContext - queries and updates to Data
 * @property mainContext CoroutineContext - changes tu UI
 */
abstract class BaseViewModel : ViewModel() {

    protected val superJob = SupervisorJob()
    protected val ioContext = Job(superJob) + Dispatchers.IO
    protected val mainContext = Job(superJob) + Dispatchers.Main


    override fun onCleared() {
        superJob.cancelChildren()
    }
}
