package za.co.xisystems.itis_rrm.ui.snapcapture.gallery

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class CarouselViewModelFactory(
    private val userRepository: UserRepository,
    private val capturedPictureRepository: CapturedPictureRepository,
    var application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CarouselViewModel(
            this.userRepository,
            this.capturedPictureRepository,
            application
        ) as T
    }
}
