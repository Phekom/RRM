package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val Db: AppDatabase,
    val context: Context
) : ViewModel() {

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

    suspend fun fetchAllData(userId: String): Boolean {

        return withContext(Dispatchers.IO) {
            offlineDataRepository.fetchAllData(userId)
        }

//        val job = Job()
//        val ioContext = Dispatchers.IO + uncaughtExceptionHandler + job
//        val time = measureTimeMillis {
//            viewModelScope.launch(Dispatchers.Main) {
//
//                val entities = async(ioContext) {
//                    offlineDataRepository.getAllEntities()
//                    7
//                }
//
//
//                val lookups = async(ioContext) {
//                    offlineDataRepository.refreshLookups(userId)
//                }
//
//                val actJob = async(ioContext) {
//                    offlineDataRepository.refreshActivitySections(userId)
//                }
//
//                val workflows = async(ioContext) {
//                    offlineDataRepository.refreshWorkflows(userId)
//                }
//
//                val taskList = async(ioContext) {
//                    offlineDataRepository.fetchUserTaskList(userId)
//
//                }
//
//                val contracts = async(ioContext) {
//                    offlineDataRepository.refreshContractInfo(userId)
//                }
//
//                try {
//                    val result =
//                        entities.await() + lookups.await() + actJob.await() + workflows.await() + taskList.await() + contracts.await()
//                    Timber.d("$result")
//                    withContext(Dispatchers.Main) {
//                        setFetchResult(true)
//                    }
//                } catch (e: Exception) {
//                    setFetchResult(false)
//                    Timber.e(e, "Failed to Get All Data.")
//                    throw e
//                }
//
//            }
//
//        }
//        Timber.d("Time taken: $time")


    }
}
