package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : BaseViewModel() {
    private val sJob = SupervisorJob()
    private var databaseStatus: LiveData<XIEvent<XIResult<Boolean>>> =
        offlineDataRepository.databaseStatus.distinctUntilChanged()

    var databaseState: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    init {
        viewModelScope.launch(Job(sJob) + uncaughtExceptionHandler + Dispatchers.Main) {
            databaseStatus.observeForever { it ->
                it?.getContentIfNotHandled()?.let {
                    databaseState.postValue(it)
                }
            }
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
        viewModelScope.launch(Job(sJob) + uncaughtExceptionHandler + Dispatchers.Main) {

            val fetchJob = Job()

            val jobContext = fetchJob + Dispatchers.IO + uncaughtExceptionHandler

            try {
                this.launch {
                    Coroutines.main {
                        databaseState.postValue(XIProgress(true))
                    }
                    val loadContracts = async(jobContext) {
                        offlineDataRepository.loadActivitySections(userId)
                        offlineDataRepository.loadContracts(userId)
                    }
                    val loadLookups = async(jobContext) { offlineDataRepository.loadLookups(userId) }
                    val loadActivities = async(jobContext) {
                        offlineDataRepository.loadTaskList(userId)
                        offlineDataRepository.loadWorkflows(userId)
                    }
                    loadContracts.await()
                    loadLookups.await()
                    loadActivities.await()
                    databaseState.postValue(XIStatus("Download complete"))
                    fetchJob.complete()
                }
            } catch (t: Throwable) {
                fetchJob.completeExceptionally(t)
                jobContext.cancelChildren(CancellationException(t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR))
                val fetchFail =
                    XIError(t, "Failed to fetch contracts: ${t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR}")

                databaseState.postValue(fetchFail)
            } finally {
                databaseState.postValue(XIProgress(false))
            }

        }

    override fun onCleared() {
        scope.cancel()
        sJob.cancelChildren()
        super.onCleared()
    }

    suspend fun healthCheck(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getServiceHealth(userId)
        }
    }
}
