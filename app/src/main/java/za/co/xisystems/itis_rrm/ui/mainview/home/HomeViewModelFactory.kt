package za.co.xisystems.itis_rrm.ui.mainview.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    private val application: Application
): ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, offlineDataRepository, dispatchers, application) as T
    }
}
