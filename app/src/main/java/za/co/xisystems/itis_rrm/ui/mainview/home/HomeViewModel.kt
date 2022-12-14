/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/14 7:49 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.ConnectException
import za.co.xisystems.itis_rrm.custom.errors.TransmissionException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : AndroidViewModel(application) {

    private val superJob = SupervisorJob()
    private var databaseStatus: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    private var ioContext = dispatchers.io() + Job(superJob)
    private var mainContext = dispatchers.main() + Job(superJob)
    var healthState: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    var databaseState: MutableLiveData<XIResult<Boolean>?> = MutableLiveData()
    var versionState: MutableLiveData<XIResult<String>?> = MutableLiveData()

    val user by lazyDeferred {
        repository.getUser()
    }
    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }
    var bigSyncDone: MutableLiveData<Boolean> = MutableLiveData()

    init {
        viewModelScope.launch(mainContext) {

            databaseStatus = offlineDataRepository.databaseStatus

            databaseState = Transformations.map(databaseStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<Boolean>?>
        }
    }

    fun bigSyncCheck() = viewModelScope.launch(ioContext) {
        val result = offlineDataRepository.bigSyncCheck()
        withContext(mainContext) {
            bigSyncDone.value = result
        }
    }

    fun fetchAllData(userId: String) = viewModelScope.launch(ioContext, CoroutineStart.DEFAULT) {
        try {
            offlineDataRepository.loadActivitySections(userId)
            offlineDataRepository.loadContracts(userId)
            offlineDataRepository.loadLookups(userId)
            offlineDataRepository.loadTaskList(userId)
            offlineDataRepository.loadWorkflows(userId)
            withContext(mainContext) {
                databaseState.postValue(XIResult.Success(true))
            }

        } catch (t: Throwable) {
            val fetchFail = XIResult.Error( t, "Failed to fetch contracts:" + " ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
            databaseState.postValue(fetchFail)
        } finally {
            withContext(mainContext) {
                ioContext.cancelChildren(
                    CancellationException("Failed to fetch contracts:" ?: XIErrorHandler.UNKNOWN_ERROR)
                )
            }
        }
//        try {
//       offlineDataRepository.loadActivitySections(userId)
//            offlineDataRepository.loadContracts(userId)
//            offlineDataRepository.loadLookups(userId)
//            offlineDataRepository.loadTaskList(userId)
//            offlineDataRepository.loadWorkflows(userId)
//            withContext(mainContext) {
//                databaseState.postValue(XIResult.Success(true))
//            }
//        } catch (exception: Exception) {
//            Timber.e(exception, "Failed to synch contracts")
//            withContext(mainContext) {
//                ioContext.cancelChildren(
//                    CancellationException(exception.message ?: XIErrorHandler.UNKNOWN_ERROR, exception)
//                )
//                val fetchFail =
//                    XIResult.Error(
//                        exception,
//                        "Failed to fetch contracts:" +
//                                " ${exception.message ?: XIErrorHandler.UNKNOWN_ERROR}"
//                    )
//                databaseState.postValue(fetchFail)
//            }
//        }
    }





    fun healthCheck() = viewModelScope.launch(ioContext) {
//        try {
//            val result = offlineDataRepository.getServiceHealth()
//            withContext(mainContext) {
//                healthState.value = XIEvent(XIResult.Success(result))
//            }
//        } catch (t: Throwable) {
//            failValidation()
//        } finally {
//            failValidation()
//        }
        try {
            val result = offlineDataRepository.getServiceHealth()
            withContext(mainContext) {
                healthState.value = XIEvent(XIResult.Success(result))
            }
        } catch (t: Throwable) {
            val message = "Health check failed: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            withContext(mainContext) {
                healthState.value = XIEvent(XIResult.Error(t, message))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        superJob.cancelChildren(CancellationException("viewmodel cleared"))
        databaseState = MutableLiveData()
        databaseStatus = MutableLiveData()
        healthState = MutableLiveData()
        bigSyncDone = MutableLiveData()
    }

    fun resetSyncStatus() {
        bigSyncDone = MutableLiveData()
    }

    fun failValidation() = viewModelScope.launch(mainContext) {
        healthState.value =
            XIEvent(
                XIResult.Error(
                    TransmissionException(
                        "Server Communication Error", ConnectException("You Have No Active Data")
                    ),
                    "You May have Ran out of Data Please Check and Retry.","You Have No Active Data Connection"
                )
            )
    }

}
