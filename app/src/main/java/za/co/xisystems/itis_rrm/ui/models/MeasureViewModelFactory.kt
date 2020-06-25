package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class MeasureViewModelFactory(
    application: Application,
    private val measureCreationDataRepository: MeasureCreationDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {
    private var mApplication = application
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MeasureViewModel(
            mApplication,
            measureCreationDataRepository,
            offlineDataRepository
        ) as T
    }
}
