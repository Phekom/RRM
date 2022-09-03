package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.decline_job

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class DeclineJobViewModelFactory(
    private val offlineDataRepository: OfflineDataRepository,
    private val photoUtil: PhotoUtil,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeclineJobViewModel(
            offlineDataRepository,photoUtil,
        ) as T
    }
}
