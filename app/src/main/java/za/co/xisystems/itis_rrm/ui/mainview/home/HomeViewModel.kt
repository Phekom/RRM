package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    private val databaseStatus: LiveData<XIResult<Boolean>> = offlineDataRepository.databaseStatus

    val databaseState: MutableLiveData<XIResult<Boolean>> = Transformations.map(databaseStatus) { status ->
        status
    } as MutableLiveData<XIResult<Boolean>>

    private var job: Job = Job()

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
