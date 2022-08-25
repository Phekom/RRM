
/**
 * Created by Francis Mahlava on 2021/11/23.
 */

package za.co.xisystems.itis_rrm.utils.image_capture

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.ui.start.SplashActivityViewModel
import za.co.xisystems.itis_rrm.utils.PhotoUtil

@Suppress("UNCHECKED_CAST")
class ImagePickerViewModelFactory(
    private val unallocatedRepository: CapturedPictureRepository, private val application: Application, private val photoUtil: PhotoUtil
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ImagePickerViewModel(unallocatedRepository = unallocatedRepository,application,photoUtil) as T
    }
}