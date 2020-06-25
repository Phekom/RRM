package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class ApproveJobsViewModelFactory(
//    private val repository: UserRepository,
    private val jobApprovalDataRepository: JobApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ApproveJobsViewModel(jobApprovalDataRepository, offlineDataRepository) as T
//        return MeasureViewModel(repository,,Db ,context) as T
    }
}
