package za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class GoToViewModelFactory(
    private val dataRepository: WorkDataRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GoToViewModel(dataRepository) as T
    }
}
