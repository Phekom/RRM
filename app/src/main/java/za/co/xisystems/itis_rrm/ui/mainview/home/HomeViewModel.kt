/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/14 7:49 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    application: Application
) : AndroidViewModel(application) {
    private val superJob = SupervisorJob()
    private var databaseStatus: LiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    private var homeIoContext = Dispatchers.IO + Job(superJob)
    private var homeMainContext = Dispatchers.Main + Job(superJob)
    private var healthState: MutableLiveData<XIResult<Boolean>> = MutableLiveData()
    var databaseState: MutableLiveData<XIResult<Boolean>?> = MutableLiveData()

    val user by lazyDeferred {
        repository.getUser()
    }
    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }
    val bigSyncDone: MutableLiveData<Boolean> = offlineDataRepository.bigSyncDone

    init {
        viewModelScope.launch(homeMainContext) {

            databaseStatus = offlineDataRepository.databaseStatus

            databaseState = Transformations.map(databaseStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<Boolean>?>
        }
    }

    suspend fun bigSyncCheck() {
        offlineDataRepository.bigSyncCheck()
    }

    suspend fun fetchAllData(userId: String) =
        viewModelScope.launch(homeMainContext) {

            try {
                withContext(homeIoContext + uncaughtExceptionHandler) {
                    val contractJob = async(homeIoContext) {
                        offlineDataRepository.loadActivitySections(userId)
                        offlineDataRepository.loadLookups(userId)
                        offlineDataRepository.loadContracts(userId)
                        offlineDataRepository.loadTaskList(userId)
                        offlineDataRepository.loadWorkflows(userId)
                    }
                    contractJob.await()
                    databaseState.postValue(XIResult.Success(true))
                }
            } catch (exception: Exception) {
                withContext(homeMainContext) {
                    homeIoContext.cancelChildren(
                        CancellationException(exception.message ?: XIErrorHandler.UNKNOWN_ERROR, exception)
                    )
                    val fetchFail =
                        XIResult.Error(
                            exception, "Failed to fetch contracts:" +
                                " ${exception.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                        )
                    databaseState.postValue(fetchFail)
                }
            }
        }

    suspend fun healthCheck(userId: String): Boolean {
        return withContext(homeIoContext) {
            try {
                offlineDataRepository.getServiceHealth(userId)
            } catch (t: Throwable) {
                withContext(homeMainContext) {
                    val message = "Health check failed: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                    healthState.postValue(XIResult.Error(t, message))
                }
                false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        superJob.cancelChildren(CancellationException("viewmodel cleared"))
        databaseState = MutableLiveData()
        databaseStatus = MutableLiveData()
    }
}
