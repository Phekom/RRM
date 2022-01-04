package za.co.xisystems.itis_rrm.ui.mainview.work

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class WorkViewModelFactory(
    private val workDataRepository: WorkDataRepository,
    private val offlineDataRepository: OfflineDataRepository,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkViewModel(application, workDataRepository, offlineDataRepository) as T
    }
}
