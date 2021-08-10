package za.co.xisystems.itis_rrm.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class DeferredLocationViewModelFactory(
    private val deferredLocationRepository: DeferredLocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeferredLocationViewModel(
            deferredLocationRepository
        ) as T
    }
}
