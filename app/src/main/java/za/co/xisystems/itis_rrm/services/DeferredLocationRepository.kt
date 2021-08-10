package za.co.xisystems.itis_rrm.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocationException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.domain.SectionBorder
import za.co.xisystems.itis_rrm.utils.Utils.round

class DeferredLocationRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
) : SafeApiRequest() {

    companion object {
        const val DISTANCE = 0.05
        const val IN_BUFFER = -1.0
    }

    @Suppress("MagicNumber")
    suspend fun validateLocation(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {
        var result: XIResult<LocationValidation> =
            XIResult.Error(
                NoDataException("No response from the service"),
                "The service is down, please try again later."
            )
        try {
            val routeSectionPointResponse =
                apiRequest { api.getRouteSectionPoint(DISTANCE, IN_BUFFER, latitude, longitude, useR) }
            with(routeSectionPointResponse) {
                Timber.d("$routeSectionPointResponse")

                if (!errorMessage.isNullOrBlank()) {
                    throw ServiceException(errorMessage)
                }

                if (linearId.contains("xxx" as CharSequence, ignoreCase = true) ||
                    bufferLocation.contains("xxx" as CharSequence, ignoreCase = true) ||
                    !errorMessage.isNullOrBlank()
                ) {
                    throw NoDataException("This photograph was not taken within 50 metres of a national road")
                } else {
                    result = saveRouteSectionPoint(
                        direction = direction,
                        linearId = linearId,
                        pointLocation = pointLocation,
                        sectionId = sectionId,
                        projectId = projectId!!,
                        jobId = jobId
                    )
                }
            }
        } catch (e: Throwable) {
            result = XIResult.error(e, "There was a problem processing this photograph: ${e.message}")
        }

        return@withContext result
    }

    @Suppress("MagicNumber")
    fun saveRouteSectionPoint(
        direction: String,
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String,
        jobId: String
    ): XIResult<LocationValidation> {
        val name = object {}.javaClass.enclosingMethod?.name
        Timber.d("x -> $name")
        if (linearId.isNotBlank()) {
            if (!appDb.getSectionPointDao().checkSectionExists(sectionId, projectId, jobId)) {
                appDb.getSectionPointDao()
                    .insertSection(direction, linearId, pointLocation, sectionId, projectId, jobId)
            }
            appDb.getProjectSectionDao().updateSectionDirection(direction, projectId)
        }

        var projectSectionId = appDb.getProjectSectionDao()
            .getSectionByRouteSectionProject(sectionId.toString(), linearId, projectId, pointLocation)

        // Deal with SectionDirection combinations.
        // S.McDonald 2021/05/14
        if (projectSectionId.isNullOrBlank()) {
            projectSectionId = appDb.getProjectSectionDao().getSectionByRouteSectionProject(
                sectionId.toString().plus(direction),
                linearId,
                projectId,
                pointLocation
            )
        }
        Timber.d("ProjectSectionId: $projectSectionId")

        return if (!projectSectionId.isNullOrBlank()) {
            val data = LocationValidation(
                routeMarker = "$linearId $sectionId $direction at ${pointLocation.round(3)} km",
                pointLocation = pointLocation,
                projectSectionId = projectSectionId,
                messages = arrayListOf()
            )

            XIResult.Success(data)
        } else {
            findNearestSection(linearId, pointLocation, direction)
        }
    }

    @Suppress("MagicNumber")
    fun findNearestSection(linearId: String, pointLocation: Double, direction: String): XIResult<LocationValidation> {
        var result = "This photograph was not taken within 50 metres of a national road"

        val closestEndKm =
            appDb.getProjectSectionDao().findClosestEndKm(linearId, pointLocation, direction)
                ?: SectionBorder("x", -1.0)

        val closestStartKm =
            appDb.getProjectSectionDao().findClosestStartKm(linearId, pointLocation, direction)
                ?: SectionBorder("x", -1.0)

        if (closestEndKm.kmMarker != -1.0 || closestStartKm.kmMarker != -1.0) {
            val distanceBack = pointLocation - closestEndKm.kmMarker
            val distanceForward = closestStartKm.kmMarker - pointLocation

            result = when {
                distanceBack < distanceForward -> {
                    "The closest section (${closestEndKm.section}) is " +
                        "$distanceBack km away at marker " +
                        "${closestEndKm.kmMarker.round(3)}."
                }
                else -> {
                    "The closest section (${closestStartKm.section}) is " +
                        "$distanceForward km away at marker " +
                        "${closestStartKm.kmMarker.round(3)}."
                }
            }
        }

        return XIResult.Error(LocationException(result), "This photograph was out of bounds")
    }
}
