package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class WorkViewModelFactory(
//    private val repository: UserRepository,
    private val workDataRepository: WorkDataRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkViewModel(workDataRepository) as T
//        return MeasureViewModel(repository,offlineDataRepository,Db ,context) as T
    }
}