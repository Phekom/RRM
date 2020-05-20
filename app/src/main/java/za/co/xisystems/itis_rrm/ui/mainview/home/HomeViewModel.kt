package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler
import kotlin.system.measureTimeMillis

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val Db: AppDatabase,
    val context: Context
) : ViewModel() {

    val offlineData by lazyDeferred {
        offlineDataRepository.getAllEntities()
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

    val fetchResult: MutableLiveData<Int>
        get() = MutableLiveData(-1)

    fun setFetchResult(weight: Int) {
        fetchResult.postValue(weight)
    }

    /**
     * Le Big Sync - Initial Database Population
     * @return Job
     */

    fun fetchAllData() = viewModelScope.launch(Dispatchers.Main) {

        val job = Job()
        val dataContext = Dispatchers.IO + uncaughtExceptionHandler + job

        val time = measureTimeMillis {
            Coroutines.main {
                val userData = this@HomeViewModel.user.await().value!!
                val jobs = listOf(
                    /*
                    async(dataContext) {
                        offlineDataRepository.getAllEntities()
                    },
                     */
                    async(dataContext) {
                        offlineDataRepository.refreshLookups(userData.userId)
                    },
                    async(dataContext) {
                        offlineDataRepository.refreshActivitySections(userData.userId)
                    },
                    async(dataContext) {
                        offlineDataRepository.refreshWorkflows(userData.userId)
                    },
                    async(dataContext) {
                        offlineDataRepository.fetchUserTaskList(userData.userId)
                    },
                    async(dataContext) {
                        offlineDataRepository.refreshContractInfo(userData.userId)
                    }
                )


                try {
                    Timber.d("Jobs Start")
                    jobs.awaitAll()
                    setFetchResult(22)
                } catch (e: Exception) {
                    setFetchResult(-1)
                    Timber.e(e, "Failed to Get All Data.")
                    throw e
                }
            }
        }
        Timber.d("Time taken: $time")


    }
}
