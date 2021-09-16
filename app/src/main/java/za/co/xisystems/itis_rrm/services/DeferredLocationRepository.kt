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
import za.co.xisystems.itis_rrm.data.network.responses.RouteSectionPointResponse
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
    suspend fun getRouteSectionPoint(
        locationQuery: LocationValidation
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {
        var result: XIResult<LocationValidation> =
            XIResult.Error(
                NoDataException("No response from the service"),
                "The service is down, please try again later."
            )
        try {

            val routeSectionPointResponse: RouteSectionPointResponse =
                apiRequest {
                    api.getRouteSectionPoint(
                        distance = DISTANCE,
                        buffer = IN_BUFFER,
                        latitude = locationQuery.latitude,
                        longitude = locationQuery.longitude,
                        userId = locationQuery.userId
                    )
                }
            with(routeSectionPointResponse) {
                Timber.d("$routeSectionPointResponse")

                if (!errorMessage.isNullOrBlank()) {
                    throw ServiceException(errorMessage)
                }

                if (linearId.contains("xxx" as CharSequence, ignoreCase = true).or(linearId.isBlank()) ||
                    bufferLocation.contains("xxx" as CharSequence, ignoreCase = true).or(bufferLocation.isBlank())

                ) {

                    throw LocationException(
                        "Coordinate (lat: ${locationQuery.latitude}, " +
                            "lng: ${locationQuery.longitude}) " +
                            "is not in Sanral territory."
                    )
                } else {
                    routeSectionPointResponse.apply {
                        val routeSectionPointResult = locationQuery.copy(
                            direction = this.direction, route = this.linearId, pointLocation = this.pointLocation,
                            sectionId = this.sectionId.toString()
                        ).setBufferLocation(this.bufferLocation)
                        result = XIResult.Success(routeSectionPointResult)
                    }
                }
            }
        } catch (e: Throwable) {
            result = XIResult.Error(e, "${e.message}")
        }

        return@withContext result
    }

    @Suppress("MagicNumber")
    suspend fun saveRouteSectionPoint(
        routeSectionQuery: LocationValidation
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {
        val name = object {}.javaClass.enclosingMethod?.name
        Timber.d("x -> $name")
        var projectSectionId: String? = null

        routeSectionQuery.apply {
            if (!route.isNullOrBlank()) {
                if (!appDb.getSectionPointDao().checkSectionExists(
                        sectionId!!.toInt(),
                        projectId,
                        jobId,
                        pointLocation!!
                    )
                ) {
                    appDb.getSectionPointDao()
                        .insertSection(
                            direction!!,
                            route!!,
                            pointLocation!!,
                            sectionId!!.toInt(),
                            projectId,
                            jobId
                        )
                }
                appDb.getProjectSectionDao().updateSectionDirection(direction, projectId)
                projectSectionId = appDb.getProjectSectionDao()
                    .getSectionByRouteSectionProject(sectionId.toString(), route!!, projectId, pointLocation!!)

                // Deal with SectionDirection combinations.
                // S.McDonald 2021/05/14
                if (projectSectionId.isNullOrBlank()) {
                    projectSectionId = appDb.getProjectSectionDao().getSectionByRouteSectionProject(
                        sectionId.toString().plus(direction),
                        route!!,
                        projectId,
                        pointLocation!!
                    )
                }
            }
        }

        Timber.d("^*^ ProjectSectionId: $projectSectionId")
        if (!projectSectionId.isNullOrBlank()) {
            return@withContext validateRouteSection(
                routeSectionQuery.projectId,
                routeSectionQuery
            )
        } else {
            return@withContext findNearestSection(
                routeSectionQuery.route!!,
                routeSectionQuery.pointLocation!!,
                routeSectionQuery.direction!!
            )
        }
    }

    @Suppress("MagicNumber")
    suspend fun findNearestSection(
        linearId: String,
        pointLocation: Double,
        direction: String
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {
        var result = "Photo falls outside of the active project scope."

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

        return@withContext XIResult.Error(LocationException(result), result)
    }

    private suspend fun validateRouteSection(
        projectId: String,
        locationValidationData: LocationValidation
    ): XIResult<LocationValidation> = withContext(Dispatchers.IO) {

        val message = "This photograph was not taken within 50 metres of a national road"
        var result: XIResult<LocationValidation> =
            XIResult.Error(LocationException(message), message)

        val sectionPoint = appDb.getSectionPointDao().getPointSectionData(projectId)

        if (!sectionPoint.projectId.isNullOrBlank()) {
            val sectionResult = locationValidationData.setSectionPoint(sectionPoint)
            val projectSectionId = appDb.getProjectSectionDao().getSectionByRouteSectionProject(
                sectionPoint.sectionId.toString(),
                sectionPoint.linearId!!,
                sectionPoint.projectId,
                sectionPoint.pointLocation
            )

            if (!projectSectionId.isNullOrBlank()) {
                val projectSection = appDb.getProjectSectionDao().getProjectSection(projectSectionId)
                projectSection.let {
                    val projectResult = sectionResult.setProjectSection(projectSection)
                    result = XIResult.Success(data = projectResult)
                }
            } else {
                result = findNearestSection(sectionPoint.linearId, sectionPoint.pointLocation, sectionPoint.direction!!)
            }
        }

        return@withContext result
    }
}
