package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class ApproveJobsViewModelFactory(
    application: Application,
    private val jobApprovalDataRepository: JobApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {
    private var mApplication: Application = application
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ApproveJobsViewModel(
            mApplication,
            jobApprovalDataRepository,
            offlineDataRepository
        ) as T
    }
}
