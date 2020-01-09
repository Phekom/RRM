package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.ViewModel
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class UnSubmittedViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
        offlineDataRepository.getContracts()
    }
}