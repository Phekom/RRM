package za.co.xisystems.itis_rrm.ui.mainview.corrections

import androidx.lifecycle.ViewModel
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class CorrectionsViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
        offlineDataRepository.getContracts()
    }
}