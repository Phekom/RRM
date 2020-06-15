package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.results.XISuccess

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val Db: AppDatabase,
    val context: Context
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

    val fetchResult: MutableLiveData<Boolean>
        get() = MutableLiveData(false)

    fun setFetchResult(flag: Boolean) {
        fetchResult.postValue(flag)
    }

    val databaseResult: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    val dataBaseStatus = scope.launch(scope.coroutineContext) {
        offlineDataRepository.databaseStatus.observeForever {
            when (it) {
                is XISuccess -> {

                    scope.launch(scope.coroutineContext) {
                        fetchContractsAndProjects(it)
                    }
                }
            }
        }
    }

    suspend fun fetchContractsAndProjects(it: XIResult<Boolean>) {
        val contracts = offlineDataRepository.getContracts().value
        val projects = offlineDataRepository.getProjects().value
        databaseResult.postValue(it)
    }

    suspend fun fetchAllData(userId: String): Boolean {

        return withContext(Dispatchers.IO) {
            offlineDataRepository.fetchContracts(userId)
        }
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
