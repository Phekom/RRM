package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.MeasureDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class MeasureViewModelFactory(
//    private val repository: UserRepository,
    private val measureDataRepository: MeasureDataRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MeasureViewModel(measureDataRepository) as T
//        return MeasureViewModel(repository,,Db ,context) as T
    }
}