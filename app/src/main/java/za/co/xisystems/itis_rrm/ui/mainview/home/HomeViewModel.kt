package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
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
        try {
            databaseState.postValue(XIProgress(true))
            offlineDataRepository.fetchContracts(userId)
        } catch (ex: Exception) {
            val fetchFail = XIError(ex, "Failed to fetch data: ${ex.message}")
            databaseState.postValue(fetchFail)
        } finally {
            databaseState.postValue(XIProgress(false))

        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    suspend fun healthCheck(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getServiceHealth(userId)
        }
    }
}
