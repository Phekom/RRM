package za.co.xisystems.itis_rrm.services

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.LocationException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.Utils.round

class DeferredLocationViewModel(
    private val deferredLocationRepository: DeferredLocationRepository,
    private val jobCreationDataRepository: JobCreationDataRepository
) : ViewModel() {

    private val superJob = SupervisorJob()
    private val ioContext = Job(superJob) + Dispatchers.IO
    private val mainContext = Job(superJob) + Dispatchers.Main
    private var _opsProgress: MutableLiveData<Boolean> = MutableLiveData()

    private val geoCodingUpdate: MutableLiveData<XIEvent<XIResult<JobDTO>>> = MutableLiveData()
    var geoCodingResult: MutableLiveData<XIResult<JobDTO>?> = MutableLiveData()
    private var errorState: Boolean = false
    val errorMessage: MutableLiveData<ColorToast> = MutableLiveData()
    val errorNavigation: MutableLiveData<NavDirections> = MutableLiveData()
    val operationalExceptions: MutableLiveData<XIResult.Error> = MutableLiveData()
    val deferredLocationStatus: MutableLiveData<XIResult<LocationValidation>> = MutableLiveData()
    private suspend fun getRouteSectionPoint(
        locationQuery: LocationValidation
    ): XIResult<LocationValidation> = withContext(ioContext) {
        return@withContext deferredLocationRepository.getRouteSectionPoint(
            locationQuery
        )
    }

    init {
        viewModelScope.launch(mainContext) {
            geoCodingResult = Transformations.map(geoCodingUpdate) { input ->
                input.getContentIfNotHandled()
            } as MutableLiveData<XIResult<JobDTO>?>
        }
    }

    suspend fun checkLocations(job: JobDTO) = withContext(mainContext) {
        this@DeferredLocationViewModel.errorState = false
        var validProjectSectionId: String? = null

        job.jobItemEstimates.forEachIndexed estimate@{ estIndex, uncheckedEstimate ->
            uncheckedEstimate.jobItemEstimatePhotos.forEachIndexed photo@{ phIndex, uncheckedPhoto ->

                val locationQuery = LocationValidation(
                    projectId = job.projectId!!,
                    jobId = job.jobId,
                    estimateId = uncheckedEstimate.estimateId,
                    userId = job.userId.toString(),
                    longitude = uncheckedPhoto.photoLongitude!!.round(8),
                    latitude = uncheckedPhoto.photoLatitude!!.round(8)
                )
                val routeSectionResponse = getRouteSectionPoint(
                    locationQuery
                )
                when (routeSectionResponse) {
                    is XIResult.Error -> {
                        processLocationResult(
                            routeSectionResponse,
                            estimatePhoto = uncheckedPhoto
                        ).also {
                            it?.let { treatedPhoto ->
                                persistChanges(uncheckedEstimate, treatedPhoto, job, estIndex, phIndex)
                            }
                        }
                        if (routeSectionResponse.exception is LocationException) {
                            return@photo
                        } else {
                            Timber.e(routeSectionResponse.exception, routeSectionResponse.message)
                            geoCodingUpdate.value = XIEvent(routeSectionResponse)
                        }
                    }
                    is XIResult.Success<LocationValidation> -> {
                        val routeSectionQuery = routeSectionResponse.data
                        val projectSectionIdResponse = deferredLocationRepository.saveRouteSectionPoint(
                            routeSectionQuery
                        )
                        processAndPersist(
                            locationResult = projectSectionIdResponse,
                            job = job,
                            uncheckedEstimate = uncheckedEstimate,
                            uncheckedPhoto = uncheckedPhoto,
                            estIndex = estIndex,
                            phIndex = phIndex
                        )
                        if (projectSectionIdResponse is XIResult.Error) {
                            if (projectSectionIdResponse.exception is LocationException) {
                                return@photo
                            } else {
                                Timber.e(projectSectionIdResponse.exception, projectSectionIdResponse.message)
                                geoCodingUpdate.value = XIEvent(projectSectionIdResponse)
                                return@estimate
                            }
                        } else if (projectSectionIdResponse is XIResult.Success && validProjectSectionId.isNullOrBlank()) {
                            validProjectSectionId = projectSectionIdResponse.data.projectSectionId!!
                        }
                    }
                    else -> {
                        Timber.d("^*^ $routeSectionResponse")
                    }
                }
            } // All photos processed
            try {
                val geoCoded = uncheckedEstimate.arePhotosGeoCoded()
                val newEstimateItem = uncheckedEstimate.copy(geoCoded = geoCoded)
                job.jobItemEstimates[estIndex] = newEstimateItem
                jobCreationDataRepository.backupEstimate(newEstimateItem)
            } catch (e: Exception) {
                val message = "Could not save updated estimate: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(e, message)
                geoCodingUpdate.value = XIEvent(XIResult.Error(e, message))
            }
        }
        // jobItemEstimates processed
        try {
            if (!validProjectSectionId.isNullOrBlank() && job.isGeoCoded()) {
                val updatedJob = updateOrCreateJobSection(job, validProjectSectionId!!)
                when (updatedJob.sectionId != null) {
                    true -> geoCodingUpdate.value = XIEvent(XIResult.Success(updatedJob))
                    else -> failLocationValidation()
                }
            } else {
                failLocationValidation()
            }
        } catch (e: Exception) {
            val message = "Failed to save geocoded job: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(e, message)
            geoCodingUpdate.value = XIEvent(XIResult.Error(e, message))
        }
    }

    private fun failLocationValidation() {
        geoCodingUpdate.value =
            XIEvent(
                XIResult.Error(
                    LocationException(
                        "One or more locations could not be validated.\n" +
                            "Check estimates for details."
                    ), "Location validation failed!"
                )
            )
    }

    private suspend fun processAndPersist(
        locationResult: XIResult<LocationValidation>,
        job: JobDTO,
        uncheckedEstimate: JobItemEstimateDTO,
        uncheckedPhoto: JobItemEstimatesPhotoDTO,
        estIndex: Int,
        phIndex: Int
    ) {
        try {
            processLocationResult(
                theResult = locationResult,
                estimatePhoto = uncheckedPhoto
            ).also {
                it?.let { treatedPhoto ->
                    persistChanges(
                        estimateItem = uncheckedEstimate,
                        treatedPhoto = treatedPhoto,
                        job = job,
                        estIndex = estIndex,
                        phIndex = phIndex
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not save local changes")
            val localDataException = LocalDataException(e.message ?: XIErrorHandler.UNKNOWN_ERROR)
            geoCodingUpdate.value =
                XIEvent(
                    XIResult.Error(
                        exception = localDataException,
                        "Could not update local data."
                    )
                )
        }
    }

    // Update the job with current estimate and photo information
    private suspend fun persistChanges(
        estimateItem: JobItemEstimateDTO,
        treatedPhoto: JobItemEstimatesPhotoDTO,
        job: JobDTO,
        estIndex: Int,
        phIndex: Int
    ): JobDTO = withContext(ioContext) {
        estimateItem.jobItemEstimatePhotos[phIndex] = treatedPhoto
        val updatedEstimate = jobCreationDataRepository.backupEstimate(estimateItem)
        job.jobItemEstimates[estIndex] = updatedEstimate
        jobCreationDataRepository.backupJob(job)
        return@withContext jobCreationDataRepository.getUpdatedJob(job.jobId)
    }

    private suspend fun processLocationResult(
        theResult: XIResult<LocationValidation>,
        estimatePhoto: JobItemEstimatesPhotoDTO,
    ): JobItemEstimatesPhotoDTO? = withContext(mainContext) {
        var treatedPhoto: JobItemEstimatesPhotoDTO? = null
        when (theResult) {
            is XIResult.Success<LocationValidation> -> {

                val result: LocationValidation = theResult.data
                treatedPhoto = estimatePhoto.copy(sectionMarker = result.routeMarker)
                treatedPhoto = if (estimatePhoto.isStartPhoto()) {
                    treatedPhoto.copy(startKm = result.pointLocation!!)
                } else {
                    treatedPhoto.copy(endKm = result.pointLocation!!)
                }
                treatedPhoto = treatedPhoto.copy(geoCoded = true)
                treatedPhoto = jobCreationDataRepository.backupEstimatePhoto(treatedPhoto)

                withContext(Dispatchers.Main) {
                    Timber.d("^*^ Good: ${theResult.data}")
                }
            }
            is XIResult.Error -> {
                if (theResult.exception is LocationException) {
                    treatedPhoto = estimatePhoto.copy(sectionMarker = theResult.message)
                    treatedPhoto = jobCreationDataRepository.backupEstimatePhoto(treatedPhoto)
                }

                withContext(Dispatchers.Main) {
                    Timber.d("^*^ Error: ${theResult.message}")
                }
            }
            else -> {
                Timber.d("No operation necessary")
            }
        }
        return@withContext treatedPhoto
    }

    /**
     * If necessary, create a new job section for the current job. Otherwise, just update the
     * existing job section.
     *
     * @param job JobDTO
     * @param projectSectionId String
     * @return JobDTO?
     */
    private suspend fun updateOrCreateJobSection(
        job: JobDTO,
        projectSectionId: String
    ): JobDTO = withContext(mainContext) {
        return@withContext try {
            val projectSection = jobCreationDataRepository.getSection(projectSectionId)
            if (!jobCreationDataRepository
                    .checkIfJobSectionExistForJobAndProjectSection(
                        jobId = job.jobId,
                        projectSectionId = projectSection.sectionId
                    )
            ) {
                val newJobSections = job.jobSections
                val newJobSection = createJobSection(projectSection, job)
                newJobSections.add(newJobSection)
                job.jobSections = newJobSections
            }

            job.sectionId = projectSection.sectionId
            job.startKm = projectSection.startKm
            job.endKm = projectSection.endKm
            jobCreationDataRepository.backupJob(job)

            // jobCreationDataRepository.updateNewJob(
            //     newJobId = job.jobId,
            //     startKM = projectSection.startKm,
            //     endKM = projectSection.endKm,
            //     sectionId = projectSection.sectionId,
            //     newJobItemEstimatesList = job.jobItemEstimates,
            //     jobItemSectionArrayList = job.jobSections
            // )
            Timber.d("^*^ Job section written for ${job.descr}")
            jobCreationDataRepository.getUpdatedJob(job.jobId)
        } catch (e: Exception) {
            Timber.e(e, "Could not save updated job.")
            throw e
        }
    }

    private suspend fun createJobSection(
        localProjectSection: ProjectSectionDTO,
        localJob: JobDTO
    ) = withContext(ioContext) {
        JobSectionDTO(
            jobSectionId = SqlLitUtils.generateUuid(),
            projectSectionId = localProjectSection.sectionId,
            jobId = localJob.jobId,
            startKm = localProjectSection.startKm,
            endKm = localProjectSection.endKm,
            recordSynchStateId = 0,
            recordVersion = 1
        ).also {
            jobCreationDataRepository.saveJobSection(it)
            return@withContext it
        }
    }

    private suspend fun updateOrCreateJobSection(jobId: String, projectSectionId: String) = withContext(ioContext) {
        val targetJob = jobCreationDataRepository.getUpdatedJob(jobId)
        return@withContext updateOrCreateJobSection(targetJob, projectSectionId)
    }

    private suspend fun handleLocationError(
        locationError: XIResult.Error
    ) = withContext(ioContext) {
        Timber.d(locationError.exception, "^*^ Deferred Location: ${locationError.message}")
    }
}

data class LocationValidation(
    var projectId: String,
    var jobId: String,
    var userId: String,
    var estimateId: String? = null,
    var photoId: String? = null,
    var routeMarker: String? = null,
    var pointLocation: Double? = null,
    var projectSectionId: String? = null,
    var longitude: Double,
    var latitude: Double,
    var direction: String? = null,
    var route: String? = null,
    var section: String? = null,
    var sectionStartKm: Double? = null,
    var sectionEndKm: Double? = null,
    var sectionId: String? = null,
    var bufferLocations: ArrayList<String> = ArrayList()

) : Parcelable {
    constructor(parcel: Parcel) : this(
        projectId = parcel.readString()!!,
        jobId = parcel.readString()!!,
        userId = parcel.readString()!!,
        estimateId = parcel.readString(),
        photoId = parcel.readString(),
        routeMarker = parcel.readString(),
        pointLocation = parcel.readValue(Double::class.java.classLoader) as? Double,
        projectSectionId = parcel.readString(),
        longitude = parcel.readDouble(),
        latitude = parcel.readDouble(),
        direction = parcel.readString(),
        route = parcel.readString(),
        section = parcel.readString(),
        sectionStartKm = parcel.readValue(Double::class.java.classLoader) as? Double,
        sectionEndKm = parcel.readValue(Double::class.java.classLoader) as? Double,
        sectionId = parcel.readString(),

        )

    fun init(
        longitude: Double,
        latitude: Double,
        userId: String,
        projectId: String,
        jobId: String
    ): LocationValidation {
        return this.copy(
            longitude = longitude,
            latitude = latitude,
            userId = userId,
            projectId = projectId,
            jobId = jobId
        )
    }

    fun setBufferLocation(bufferLocation: String): LocationValidation {
        val bufferLocations = bufferLocation.split(';') as ArrayList<String>
        return this.copy(bufferLocations = bufferLocations)
    }

    fun setSectionPoint(sectionPoint: SectionPointDTO): LocationValidation {
        return this.copy(
            pointLocation = sectionPoint.pointLocation,
            direction = sectionPoint.direction,
            route = sectionPoint.linearId,
            sectionId = sectionPoint.sectionId.toString(),
        )
    }

    fun setProjectSection(projectSection: ProjectSectionDTO): LocationValidation {
        return this.copy(
            section = projectSection.section,
            direction = projectSection.direction,
            route = projectSection.route,
            projectSectionId = projectSection.sectionId,
            sectionStartKm = projectSection.startKm,
            sectionEndKm = projectSection.endKm,
            sectionId = projectSection.sectionId
        ).setRouteMarker()
    }

    private fun setRouteMarker(): LocationValidation {
        val routeMarker = "${this.route} ${this.section} ${this.direction} at ${pointLocation?.round(3)} km"
        return this.copy(routeMarker = routeMarker)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(jobId)
        parcel.writeString(userId)
        parcel.writeString(estimateId)
        parcel.writeString(photoId)
        parcel.writeString(routeMarker)
        parcel.writeValue(pointLocation)
        parcel.writeString(projectSectionId)
        parcel.writeDouble(longitude)
        parcel.writeDouble(latitude)
        parcel.writeString(direction)
        parcel.writeString(route)
        parcel.writeString(section)
        parcel.writeValue(sectionStartKm)
        parcel.writeValue(sectionEndKm)
        parcel.writeString(sectionId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationValidation> {
        override fun createFromParcel(parcel: Parcel): LocationValidation {
            return LocationValidation(parcel)
        }

        override fun newArray(size: Int): Array<LocationValidation?> {
            return arrayOfNulls(size)
        }
    }
}
