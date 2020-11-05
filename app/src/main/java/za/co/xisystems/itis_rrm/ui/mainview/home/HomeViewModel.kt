package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
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

    private var databaseStatus: MutableLiveData<XIResult<Boolean>> = offlineDataRepository.databaseStatus
    var databaseState: MutableLiveData<XIResult<Boolean>> = MutableLiveData()
    private var job: Job = Job()

    init {
        databaseStatus.observeForever {
            viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {
                it?.let {
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

    suspend fun fetchAllData(userId: String) = viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {

        val fetchJob = Job()
        databaseState.postValue(XIProgress(true))

        val jobContext = fetchJob + Dispatchers.Default + uncaughtExceptionHandler

        try {
            viewModelScope.launch {
                val fetcher = async(jobContext) {
                    offlineDataRepository.loadActivitySections(userId)
                    offlineDataRepository.loadLookups(userId)
                    offlineDataRepository.loadTaskList(userId)
                    offlineDataRepository.loadWorkflows(userId)
                    offlineDataRepository.loadContracts(userId)
                }

                fetcher.join()

            }
        } catch (t: Throwable) {
            jobContext.cancelChildren(CancellationException(t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR))
            val fetchFail = XIError(t, "Failed to fetch data: ${t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR}")
            databaseState.postValue(fetchFail)
        } finally {
            databaseState.postValue(XIProgress(false))
        }
    }

    override fun onCleared() {
        scope.cancel()
        job.cancelChildren()
        super.onCleared()
    }

    suspend fun healthCheck(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getServiceHealth(userId)
        }
    }
}
