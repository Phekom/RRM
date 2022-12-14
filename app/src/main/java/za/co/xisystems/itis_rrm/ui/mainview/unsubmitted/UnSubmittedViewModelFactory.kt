package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class UnSubmittedViewModelFactory(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UnSubmittedViewModel(
            offlineDataRepository
        ) as T
    }
}
