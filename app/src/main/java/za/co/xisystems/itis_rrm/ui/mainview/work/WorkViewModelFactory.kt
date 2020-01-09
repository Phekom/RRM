package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class WorkViewModelFactory(
//    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
//    private val Db : AppDatabase,
//    val context: Context
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkViewModel(offlineDataRepository) as T
//        return MeasureViewModel(repository,offlineDataRepository,Db ,context) as T
    }
}