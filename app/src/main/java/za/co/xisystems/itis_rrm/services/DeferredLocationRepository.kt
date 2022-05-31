package za.co.xisystems.itis_rrm.services

import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocationException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.RouteSectionPointResponse
import za.co.xisystems.itis_rrm.domain.SectionBorder
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.Utils.round
import kotlin.math.abs

class DeferredLocationRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {

    companion object {
        const val DISTANCE = 100.0
        const val IN_BUFFER = -1.0
    }


    @Suppress("MagicNumber", "TooGenericExceptionCaught")
    suspend fun getRouteSectionPoint(
        locationQuery: LocationValidation
    ): XIResult<LocationValidation> = withContext(dispatchers.io()) {
        var result: XIResult<LocationValidation>? = null

        try {
            val sectiondata = appDb.getProjectSectionDao().getSection(locationQuery.sectionId!!)
            val routeSectionPointResponse: RouteSectionPointResponse =
                apiRequest {
                    api.getRouteSectionPoint(
                        distance = DISTANCE,
                        buffer = IN_BUFFER,
                        latitude = locationQuery.latitude,
                        longitude = locationQuery.longitude,
                        linearId = sectiondata.route!!,
                        sectionId = sectiondata.section!!,
                        direction = sectiondata.direction!!,
                        userId = locationQuery.userId
                    )
                }

            if (routeSectionPointResponse.errorMessage.isNullOrBlank()) {
                // Interim fix until buffered routes


//                val bufferLocations = routeSectionPointResponse.bufferLocation.split(";", ignoreCase = true, limit = 0)
//                    .map { item -> item.trim() }.firstOrNull { item ->
//                        !item.contains("XXXX" as CharSequence, ignoreCase = true) && item != ""
//                    }
//                if (routeSectionPointResponse.linearId.contains("XXX" as CharSequence, ignoreCase = true).or(routeSectionPointResponse.linearId.isBlank())
//                        .or(bufferLocations.isNullOrEmpty())
//                ) {
//                    throw  notEvenWrongException()
//                } else {
//                    routeSectionPointResponse.apply {
//                        val routeSectionPointResult = locationQuery.copy(
//                            direction = this.direction, route = this.linearId, pointLocation = this.pointLocation,
//                            sectionId = this.sectionId.toString()
//                        ).setBufferLocation(this.bufferLocation)
//                        result = XIResult.Success(routeSectionPointResult)
//                    }
//                }

                if (routeSectionPointResponse.linearId == null ) { //&& routeSectionPointResponse.bufferLocation.contains("XXXX")
                    throw  notEvenCloseException(routeSectionPointResponse.distanceParameter)
                } else if (routeSectionPointResponse.linearId != (sectiondata.route)) {
                    throw  wrongRoadDetectedException(routeSectionPointResponse.linearId, sectiondata.route!!)
                } else if (routeSectionPointResponse.sectionId.toString() != (sectiondata.section)) {
                    throw  wrongSectionException(routeSectionPointResponse.sectionId, routeSectionPointResponse.linearId, sectiondata.section!!, sectiondata.route)
                }
                else if (routeSectionPointResponse.direction != (sectiondata.direction)) {
                    throw  wrongDirectionException(routeSectionPointResponse.direction, sectiondata.direction)
                } else {
                    routeSectionPointResponse.apply {
                        val routeSectionPointResult = locationQuery.copy(
                            direction = this.direction, route = this.linearId, pointLocation = this.pointLocation,
                            sectionId = this.sectionId.toString()
                        ).setBufferLocation(this.bufferLocation)
                        result = XIResult.Success(routeSectionPointResult)
                    }
                }

            } else {
                if (routeSectionPointResponse.errorMessage.contains("Point not on line" as CharSequence, ignoreCase = true)) {
                    throw notEvenWrongException()
                } else {
                    throw ServiceException(routeSectionPointResponse.errorMessage)
                }
            }


        } catch (e: Throwable) {
            result = XIResult.Error(e, "${e.message}")
        }

        return@withContext result!!
    }


    private fun wrongRoadDetectedException(linearId: String, route: String): LocationException {
        return LocationException("You are on $linearId and you Selected $route")
    }

    private fun wrongSectionException(section: Int, linearId: String, yourSection: String, route: String): LocationException {
        return LocationException("Photo Captured on $linearId Section $section and you Selected $route Section $yourSection")
    }

    private fun wrongDirectionException(direction: String, yourDirection: String?): LocationException {
        return LocationException("You are on the Wrong Traffic Flow Direction ($direction)")
    }

    private fun notEvenWrongException(): LocationException {
        return LocationException("One or more images not within your allocated road reserve.")
    }

    private fun notEvenCloseException(distanceParameter: String): LocationException {
        return LocationException("One or more images not within your allocated road reserve. Tried at $distanceParameter Buffer Distance")
    }

    @Suppress("MagicNumber")
    suspend fun saveRouteSectionPoint(
        routeSectionQuery: LocationValidation
    ): XIResult<LocationValidation> = withContext(dispatchers.io()) {
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
//                if (!appDb.getProjectSectionDao().checkSectionExists(section.sectionId)) {
//                    appDb.getProjectSectionDao().updateSectionDirection(direction, projectId)
//                }

                projectSectionId = appDb.getProjectSectionDao()
                    .getSectionByRouteSectionProject(sectionId.toString(), route!!, projectId, pointLocation!!)

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
            return@withContext validateRouteSection(routeSectionQuery.projectId, routeSectionQuery )
        } else {
            return@withContext findNearestSection(routeSectionQuery.route!!, routeSectionQuery.pointLocation!!,
                routeSectionQuery.direction!! )
        }
    }

    @Suppress("MagicNumber")
    suspend fun findNearestSection(
        linearId: String,
        pointLocation: Double,
        direction: String
    ): XIResult<LocationValidation> = withContext(dispatchers.io()) {
        var result = "Photo falls outside of the active project scope."

        val closestEndKm =
            appDb.getProjectSectionDao().findClosestEndKm(linearId, pointLocation, direction)
                ?: SectionBorder("x", -1.0)

        val closestStartKm =
            appDb.getProjectSectionDao().findClosestStartKm(linearId, pointLocation, direction)
                ?: SectionBorder("x", -1.0)

        if (closestEndKm.kmMarker != -1.0 || closestStartKm.kmMarker != -1.0) {
            val distanceBack = abs(pointLocation - closestEndKm.kmMarker)
            val distanceForward = abs(closestStartKm.kmMarker - pointLocation)

            result = when {
                distanceBack < distanceForward -> {
                    "The closest section (${closestEndKm.section}) is " +
                            "${"%.3f".format(distanceBack)} km away at marker " +
                            "${closestEndKm.kmMarker.round(3)}."
                }
                else -> {
                    "The closest section (${closestStartKm.section}) is " +
                            "${"%.3f".format(distanceForward)}  km away at marker " +
                            "${closestStartKm.kmMarker.round(3)}."
                }
            }
        }

        return@withContext XIResult.Error(
            LocationException(result),
            result
        )
    }

    private suspend fun validateRouteSection(
        projectId: String,
        locationValidationData: LocationValidation
    ): XIResult<LocationValidation> = withContext(dispatchers.io()) {

        val message = "This photograph was not taken within 50 metres of a national road"
        var result: XIResult<LocationValidation> =
            XIResult.Error(
                LocationException(message),
                message
            )

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
