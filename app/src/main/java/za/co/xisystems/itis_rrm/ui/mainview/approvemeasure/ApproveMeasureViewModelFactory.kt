package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class ApproveMeasureViewModelFactory(
//    private val repository: UserRepository,
    private val measureApprovalDataRepository: MeasureApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
//    val context: Context
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ApproveMeasureViewModel(measureApprovalDataRepository,offlineDataRepository) as T
//        return MeasureViewModel(repository,,Db ,context) as T
    }
}