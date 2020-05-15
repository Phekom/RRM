package za.co.xisystems.itis_rrm.ui.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import kotlin.system.measureTimeMillis

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    val context: Context
) : ViewModel() {

    init {

    }
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


    val fetchResult = MutableLiveData<Long>()

    suspend fun fetchAllData(userId: String) {

        val time = measureTimeMillis {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {

                    val actJob = async {
                        offlineDataRepository.refreshActivitySections(userId)
                    }

                    val workflows = async {
                        offlineDataRepository.refreshWorkflows(userId)
                    }

                    val lookups = async {
                        offlineDataRepository.refreshLookups(userId)
                    }

                    val taskList = async {
                        offlineDataRepository.fetchUserTaskList(userId)

                    }

                    val contracts = async {
                        offlineDataRepository.refreshContractInfo(userId)
                    }

                    val result =
                        actJob.await() + workflows.await() + lookups.await() + taskList.await() + contracts.await()
                    Timber.d("$result")
                }
            }

        }
        Timber.d("Time taken: $time")
        fetchResult.postValue(time)

    }

}