package za.co.xisystems.itis_rrm.ui.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val Db : AppDatabase,
    val context: Context
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(
            repository,
            offlineDataRepository,
            context
        ) as T
    }
}