package za.co.xisystems.itis_rrm.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository

@Suppress("UNCHECKED_CAST")
class DeferredLocationViewModelFactory(
    private val deferredLocationRepository: DeferredLocationRepository,
    private val jobCreationDataRepository: JobCreationDataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeferredLocationViewModel(
            deferredLocationRepository,
            jobCreationDataRepository
        ) as T
    }
}
