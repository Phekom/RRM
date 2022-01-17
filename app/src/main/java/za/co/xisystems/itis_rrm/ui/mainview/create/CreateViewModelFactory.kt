package za.co.xisystems.itis_rrm.ui.mainview.create

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CreateViewModelFactory(
    private val capturedPictureRepository: CapturedPictureRepository,
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    private val photoUtil: PhotoUtil,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateViewModel(
            capturedPictureRepository = capturedPictureRepository,
            jobCreationDataRepository = jobCreationDataRepository,
            userRepository = userRepository,
            application = application,
            photoUtil = photoUtil
        ) as T
    }
}
