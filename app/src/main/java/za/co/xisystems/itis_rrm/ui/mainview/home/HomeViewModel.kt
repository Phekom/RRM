package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : BaseViewModel() {

    private var databaseStatus: LiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()

    private val superJob = SupervisorJob()

    private var homeIoContext = Dispatchers.IO + Job(superJob)
    private var homeMainContext = Dispatchers.Main + Job(superJob)
    private var healthState: MutableLiveData<XIResult<Boolean>> = MutableLiveData()
    var databaseState: MutableLiveData<XIResult<Boolean>?> = MutableLiveData()

    init {
        viewModelScope.launch(homeMainContext) {

            databaseStatus = offlineDataRepository.databaseStatus.distinctUntilChanged()

            databaseState = Transformations.map(databaseStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<Boolean>?>
        }
    }

    val user by lazyDeferred {
        repository.getUser()
    }

    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }

    val bigSyncDone: MutableLiveData<Boolean> = offlineDataRepository.bigSyncDone

    suspend fun bigSyncCheck() {
        offlineDataRepository.bigSyncCheck()
    }

    suspend fun fetchAllData(userId: String) =
        viewModelScope.launch(homeMainContext) {

            try {
                withContext(homeIoContext) {
                    offlineDataRepository.loadContracts(userId)
                    offlineDataRepository.loadTaskList(userId)
                    offlineDataRepository.loadActivitySections(userId)
                    offlineDataRepository.loadLookups(userId)
                    offlineDataRepository.loadWorkflows(userId)
                    databaseState.postValue(XISuccess(true))
                }
            } catch (exception: Exception) {
                withContext(homeMainContext) {
                    homeIoContext.cancelChildren(
                        CancellationException(exception.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    )
                    val fetchFail =
                        XIError(
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
                    healthState.postValue(XIError(t, message))
                }
                false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        superJob.cancelChildren()
        databaseState = MutableLiveData()
        databaseStatus = MutableLiveData()
    }
}
