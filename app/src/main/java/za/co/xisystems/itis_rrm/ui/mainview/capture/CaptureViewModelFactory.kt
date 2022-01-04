package za.co.xisystems.itis_rrm.ui.mainview.capture

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class CaptureViewModelFactory(
    private val userRepository: UserRepository,
    private val capturedPicsRepository: CapturedPictureRepository,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CaptureViewModel(
            userRepository = userRepository,
            capturedPicsRepository = capturedPicsRepository,
            application = application
        ) as T
    }
}
