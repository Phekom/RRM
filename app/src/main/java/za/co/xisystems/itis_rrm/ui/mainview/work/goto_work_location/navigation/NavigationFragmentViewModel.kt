package za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.navigation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class NavigationFragmentViewModel(
    private val dataRepository: WorkDataRepository,
) : ViewModel() {

    val route = MutableLiveData<DirectionsRoute>()
    fun createNewRoute(job: DirectionsRoute?) {
        route.postValue(job!!)
    }
}
