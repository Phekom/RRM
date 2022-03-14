package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.create

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import kotlin.coroutines.CoroutineContext

class CreationViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ) : ViewModel() {

    private val superJob = SupervisorJob()
    private var ioContext: CoroutineContext = Job(superJob) + dispatchers.io()
    private var mainContext: CoroutineContext = Job(superJob) + Dispatchers.Main
    var healthState: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()

    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
    }

    suspend fun getContractSelectors(): LiveData<List<ContractSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getContractSelectors()
            withContext(mainContext) {
                emit(data)
            }
        }
    }

    suspend fun getProjectSelectors(contractId: String): LiveData<List<ProjectSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getProjectSelectors(contractId)
            withContext(mainContext) {
                emit(data)
            }
        }
    }


    suspend fun backupJob(job: JobDTO) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.backupJob(job)
//        withContext(mainContext) {
//            jobId.value = job.jobId
//            setJobToEdit(job.jobId)
//        }
    }

    fun healthCheck() = viewModelScope.launch(ioContext) {
        try {
            val result = jobCreationDataRepository.getServiceHealth()
            withContext(mainContext) {
                healthState.value = XIEvent(XIResult.Success(result))
            }
        } catch (t: Throwable) {
            val message = "Health check failed: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            withContext(mainContext) {
                healthState.value = XIEvent(XIResult.Error(t, message))
            }
        }
    }


}