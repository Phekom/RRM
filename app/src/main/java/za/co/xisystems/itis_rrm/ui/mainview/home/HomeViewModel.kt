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
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : BaseViewModel() {

    private var databaseStatus: LiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()

    val superJob = SupervisorJob()

    var databaseState: MutableLiveData<XIResult<Boolean>>? = MutableLiveData()

    init {
        viewModelScope.launch(Job(superJob) + uncaughtExceptionHandler + Dispatchers.Main.immediate) {

            databaseStatus = offlineDataRepository.databaseStatus.distinctUntilChanged()

            databaseState = Transformations.map(databaseStatus) { it ->
                it?.getContentIfNotHandled()?.let {
                    it
                }
            } as? MutableLiveData<XIResult<Boolean>>
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
        viewModelScope.launch(Job(superJob) + uncaughtExceptionHandler + Dispatchers.Main.immediate) {

            val fetchJob = Job()

            val jobContext = fetchJob + Dispatchers.IO + uncaughtExceptionHandler

            try {
                databaseState?.postValue(XIProgress(true))
                viewModelScope.launch(jobContext) {
                    offlineDataRepository.loadActivitySections(userId)
                    offlineDataRepository.loadContracts(userId)
                    offlineDataRepository.loadLookups(userId)
                    offlineDataRepository.loadTaskList(userId)
                    offlineDataRepository.loadWorkflows(userId)
                }
                fetchJob.complete()
            } catch (t: Throwable) {
                fetchJob.completeExceptionally(t)
                databaseState?.postValue(XIProgress(false))
                jobContext.cancelChildren(CancellationException(t.message ?: XIErrorHandler.UNKNOWN_ERROR))
                val fetchFail =
                    XIError(t, "Failed to fetch contracts: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                databaseState?.postValue(fetchFail)
            }
        }

    suspend fun healthCheck(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getServiceHealth(userId)
        }
    }

    override fun onCleared() {
        superJob.cancelChildren()
        databaseState = MutableLiveData()

        super.onCleared()
    }
}
