
/**
 * Created by Francis Mahlava on 2021/11/23.
 */

package za.co.xisystems.itis_rrm.utils.image_capture

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.PhotoUtil

class ImagePickerViewModelFactory(
    private val application: Application,
    private val photoUtil: PhotoUtil,
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImagePickerViewModel::class.java)) {
            return ImagePickerViewModel(application,photoUtil, dispatchers) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}