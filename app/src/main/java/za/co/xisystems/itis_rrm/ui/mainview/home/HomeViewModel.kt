package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.Failure
import za.co.xisystems.itis_rrm.utils.results.Progress
import za.co.xisystems.itis_rrm.utils.results.ResultSet
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

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

    val offlineEnitities by lazyDeferred {
        offlineDataRepository.getAllEntities()
    }

    val fetchError: MutableLiveData<Failure?>
        get() = MutableLiveData(null)


    private val lastResult: MutableLiveData<ResultSet<Boolean>> = MutableLiveData(Progress(true))


    val fetchResult = lastResult

    fun setFetchResult(result: ResultSet<Boolean>) {
        lastResult.postValue(result)

    }

    val job = SupervisorJob()
    val viewModelContext = Dispatchers.Main + job + uncaughtExceptionHandler
    fun fetchAllData(userId: String) {
        viewModelScope.launch(viewModelContext) {
            withContext(Dispatchers.IO) {
                val result = offlineDataRepository.fetchAllData(userId)
                setFetchResult(result)
            }
        }
    }
}



