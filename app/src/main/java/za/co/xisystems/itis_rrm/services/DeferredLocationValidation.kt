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

    private val geoCodingUpdate: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var geoCodingResult: MutableLiveData<XIResult<String>?> = MutableLiveData()
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
            } as MutableLiveData<XIResult<String>?>
        }
    }

    suspend fun checkLocations(jobId: String) = viewModelScope.launch(mainContext) {
        val locationJob = jobCreationDataRepository.getUpdatedJob(jobId)
        this@DeferredLocationViewModel.errorState = false
        var validProjectSectionId: String? = null

        locationJob.jobItemEstimates.forEachIndexed estimate@{ estIndex, uncheckedEstimate ->
            uncheckedEstimate.jobItemEstimatePhotos.forEachIndexed photo@{ phIndex, uncheckedPhoto ->

                val locationQuery = LocationValidation(
                    projectId = locationJob.projectId!!,
                    jobId = locationJob.jobId,
                    estimateId = uncheckedEstimate.estimateId,
                    userId = locationJob.userId.toString(),
                    longitude = uncheckedPhoto.photoLongitude!!.round(8),
                    latitude = uncheckedPhoto.photoLatitude!!.round(8)
                )
                val routeSectionResponse = getRouteSectionPoint(
                    locationQuery
                )
                when (routeSectionResponse) {

                    is XIResult.Error -> {
                        processAndPersist(routeSectionResponse, uncheckedPhoto, uncheckedEstimate, locationJob, estIndex, phIndex)
                        if (routeSectionResponse.exception is LocationException) {
                            return@photo // Process the next photo
                        } else {
                            pushNonLocationException(routeSectionResponse)
                        }
                    }

                    is XIResult.Success<LocationValidation> -> {
                        val routeSectionQuery = routeSectionResponse.data
                        val projectSectionIdResponse = deferredLocationRepository.saveRouteSectionPoint(
                            routeSectionQuery
                        )
                        processAndPersist(
                            locationResult = projectSectionIdResponse,
                            job = locationJob,
                            uncheckedEstimate = uncheckedEstimate,
                            uncheckedPhoto = uncheckedPhoto,
                            estIndex = estIndex,
                            phIndex = phIndex
                        )
                        if (projectSectionIdResponse is XIResult.Error) {
                            if (projectSectionIdResponse.exception is LocationException) {
                                return@photo
                            } else {
                                pushNonLocationException(projectSectionIdResponse)
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
                val checkedEstimate = uncheckedEstimate.copy(geoCoded = geoCoded)
                locationJob.jobItemEstimates[estIndex] = checkedEstimate
                jobCreationDataRepository.backupEstimate(checkedEstimate)
            } catch (e: Exception) {
                val message = "Could not save updated estimate: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(e, message)
                geoCodingUpdate.value = XIEvent(XIResult.Error(e, message))
            }
        }
        // jobItemEstimates processed
        try {
            if (!validProjectSectionId.isNullOrBlank() && locationJob.isGeoCoded()) {
                val updatedJobId = updateOrCreateJobSection(locationJob, validProjectSectionId!!)
                if (updatedJobId.isNullOrBlank()) {
                    failLocationValidation("Failed to create job section")
                } else {
                    val checkedJob = jobCreationDataRepository.getUpdatedJob(updatedJobId)
                    if (checkedJob.sectionId.isNullOrBlank()) {
                        failLocationValidation("Failed to retrieve job section")
                    } else {
                        geoCodingUpdate.value = XIEvent(XIResult.Success(checkedJob.jobId))
                    }
                }
            } else failLocationValidation("One or more locations could not be verified.\n Please check estimates for details")
        } catch (e: Exception) {
            val message = "Failed to save geocoded job: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(e, message)
            geoCodingUpdate.value = XIEvent(XIResult.Error(e, message))
        }
    }

    private fun pushNonLocationException(routeSectionResponse: XIResult.Error) {
        Timber.e(routeSectionResponse.exception, routeSectionResponse.message)
        geoCodingUpdate.value = XIEvent(routeSectionResponse)
    }

    private fun failLocationValidation(message: String) {
        geoCodingUpdate.value =
            XIEvent(
                XIResult.Error(
                    LocationException(
                        "One or more locations could not be validated.\n" +
                            "Check estimates for details."
                    ), message
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
            processAndPersist(locationResult, uncheckedPhoto, uncheckedEstimate, job, estIndex, phIndex)
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

    private suspend fun processAndPersist(
        routeSectionResponse: XIResult<LocationValidation>,
        uncheckedPhoto: JobItemEstimatesPhotoDTO,
        uncheckedEstimate: JobItemEstimateDTO,
        locationJob: JobDTO,
        estIndex: Int,
        phIndex: Int
    ) {
        processLocationResult(
            routeSectionResponse,
            estimatePhoto = uncheckedPhoto
        ).also {
            it?.let { treatedPhoto ->
                persistChanges(uncheckedEstimate, treatedPhoto, locationJob, estIndex, phIndex)
            }
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
    ): String? = withContext(mainContext) {
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

            jobCreationDataRepository.updateNewJob(
                newJobId = job.jobId,
                startKM = projectSection.startKm,
                endKM = projectSection.endKm,
                sectionId = projectSection.sectionId,
                newJobItemEstimatesList = job.jobItemEstimates,
                jobItemSectionArrayList = job.jobSections
            )
            Timber.d("^*^ Job section written for ${job.descr}")
            job.jobId
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
