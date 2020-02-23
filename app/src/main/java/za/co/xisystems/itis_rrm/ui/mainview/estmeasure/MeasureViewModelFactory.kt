package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class MeasureViewModelFactory(
//    private val repository: UserRepository,
    private val measureCreationDataRepository: MeasureCreationDataRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MeasureViewModel(measureCreationDataRepository) as T
//        return MeasureViewModel(repository,,Db ,context) as T
    }
}