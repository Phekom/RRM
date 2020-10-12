package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
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

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : BaseViewModel() {

    val user by lazyDeferred {
        repository.getUser()
    }

    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }

    val databaseResult: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    private val dataBaseStatus: MutableLiveData<XIResult<Boolean>> = offlineDataRepository.databaseStatus

    init {
        dataBaseStatus.observeForever {
            it?.let {
                databaseResult.postValue(it)
            }
        }
    }

    val bigSyncDone: MutableLiveData<Boolean> = offlineDataRepository.bigSyncDone

    suspend fun bigSyncCheck() {
        offlineDataRepository.bigSyncCheck()
    }

    fun fetchAllData(userId: String) = scope.launch(scope.coroutineContext) {

        withContext(Dispatchers.IO) {
            try {
                databaseResult.postValue(XIProgress(true))
                offlineDataRepository.fetchContracts(userId)
            } catch (ex: Exception) {
                val fetchFail = XIError(ex, "Failed to fetch data: ${ex.message}")
                databaseResult.postValue(fetchFail)
            } finally {
                databaseResult.postValue(XIProgress(false))
            }
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
