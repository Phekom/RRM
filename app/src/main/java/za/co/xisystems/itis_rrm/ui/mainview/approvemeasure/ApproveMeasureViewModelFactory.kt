package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class ApproveMeasureViewModelFactory(
    application: Application,
    private val measureApprovalDataRepository: MeasureApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val photoUtil: PhotoUtil
) : ViewModelProvider.AndroidViewModelFactory(application) {
    private var mApplication = application
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ApproveMeasureViewModel(
            mApplication,
            measureApprovalDataRepository,
            offlineDataRepository,
            photoUtil
        ) as T
//        return MeasureViewModel(repository,,Db ,context) as T
    }
}
