package za.co.xisystems.itis_rrm.ui.start


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */


@Suppress("UNCHECKED_CAST")
class SplashActivityViewModelFactory(
    private val dataRepository: OfflineDataRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashActivityViewModel(
            dataRepository
        ) as T
    }
}