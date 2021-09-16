package za.co.xisystems.itis_rrm.ui.mainview.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CreateViewModelFactory(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateViewModel(
            jobCreationDataRepository,
            userRepository,
            application
        ) as T
    }
}
