package za.co.xisystems.itis_rrm.ui.mainview.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CreateViewModelFactory(
    private val jobCreationDataRepository: JobCreationDataRepository,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateViewModel(
            jobCreationDataRepository,
            application
        ) as T
    }
}
