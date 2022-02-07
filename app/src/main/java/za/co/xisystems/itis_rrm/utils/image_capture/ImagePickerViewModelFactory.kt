
/**
 * Created by Francis Mahlava on 2021/11/23.
 */

package za.co.xisystems.itis_rrm.utils.image_capture

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

class ImagePickerViewModelFactory(private val unallocatedRepository: CapturedPictureRepository, private val application: Application, private val photoUtil: PhotoUtil) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImagePickerViewModel::class.java)) {
            return ImagePickerViewModel(unallocatedRepository = unallocatedRepository,application,photoUtil) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}