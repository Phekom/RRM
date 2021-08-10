package za.co.xisystems.itis_rrm.services

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.results.XIResult

class DeferredLocationViewModel (private val locationRepo: DeferredLocationRepository) : ViewModel() {

    /**
     *
     * @param longitude Double
     * @param latitude Double
     */
    private suspend fun getRouteSectionPoint(
        longitude: Double,
        latitude: Double,
        userId: String,
        projectId: String,
        jobId: String
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {
        return@withContext locationRepo.validateLocation(longitude, latitude, userId, projectId, jobId)
    }
}

data class LocationValidation(
    val routeMarker: String?,
    val pointLocation: Double?,
    val projectSectionId: String?,
    val messages: ArrayList<String>
)
