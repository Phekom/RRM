package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CreationViewModelFactory(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    private val photoUtil: PhotoUtil,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreationViewModel(
            jobCreationDataRepository,
            userRepository,
            application,
            photoUtil
        ) as T
    }
}
