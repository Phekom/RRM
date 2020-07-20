package za.co.xisystems.itis_rrm.ui.mainview.home

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : LifecycleOwner, BaseViewModel() {

    val offlineData by lazyDeferred {
        offlineDataRepository.getContracts()
    }
    val user by lazyDeferred {
        repository.getUser()
    }

    val offlineWorkFlows by lazyDeferred {
        offlineDataRepository.getWorkFlows()
    }

    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }

    val fetchResult: MutableLiveData<Boolean> = MutableLiveData()

    fun setFetchResult(flag: Boolean) {
        fetchResult.postValue(flag)
    }

    val databaseResult: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    val dataBaseStatus: MutableLiveData<XIResult<Boolean>> = offlineDataRepository.databaseStatus

    val bigSyncDone: MutableLiveData<Boolean> = MutableLiveData(false)

    suspend fun bigSyncCheck() {
        bigSyncDone.postValue(offlineDataRepository.bigSyncDone())
    }

    suspend fun fetchAllData(userId: String): Boolean {

        return withContext(Dispatchers.IO) {
            try {
                offlineDataRepository.fetchContracts(userId)
                true
            } catch (ex: Exception) {
                val fetchFail = XIError(ex, "Failed to fetch data: ${ex.message}")
                dataBaseStatus.postValue(fetchFail)
                false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
        offlineDataRepository.databaseStatus.removeObservers(this)
    }

    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The lifecycle of the provider.
     */
    override fun getLifecycle(): Lifecycle {
        TODO("Not yet implemented")
    }
}
