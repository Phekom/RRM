package za.co.xisystems.itis_rrm.utils.image_capture.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CaptureImagesViewModelFactory(
    private val dataRepository: CapturedPictureRepository,
    private val photoUtil: PhotoUtil
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CaptureImagesViewModel(dataRepository, photoUtil) as T
    }
}
