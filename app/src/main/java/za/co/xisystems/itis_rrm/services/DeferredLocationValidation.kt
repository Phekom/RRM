package za.co.xisystems.itis_rrm.services

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainApp
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.LocationException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.Utils.round

class DeferredLocationViewModel(
    private val deferredLocationRepository: DeferredLocationRepository,
    private val jobCreationDataRepository: JobCreationDataRepository,
    application: Application,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val superJob = SupervisorJob()
    private val ioContext = Job(superJob) + dispatchers.io()
    private val mainContext = Job(superJob) + dispatchers.main()

    var geoCodingResult: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    private var errorState: Boolean = false
    val errorMessage: MutableLiveData<ColorToast> = MutableLiveData()


//    private suspend fun getRouteSectionPoint(
//        locationQuery: LocationValidation
//    ): RouteSectionPointResponse  = withContext(ioContext) {
//        return@withContext deferredLocationRepository.getRouteSectionPoint(
//            locationQuery
//        )
//    }

    private suspend fun getRouteSectionPoint(
        locationQuery: LocationValidation
    ):XIResult<LocationValidation>  = withContext(ioContext) { //RouteSectionPointResponse
        return@withContext deferredLocationRepository.getRouteSectionPoint(
            locationQuery
        )
    }


    init {
        geoCodingResult = MutableLiveData()
    }

    fun checkLocations(jobId: String) = viewModelScope.launch(ioContext) {

        val locationJob = jobCreationDataRepository.getUpdatedJob(jobId)
        // updated estimates from local db
        locationJob.jobItemEstimates = jobCreationDataRepository.getValidEstimatesForJobId(
            locationJob.jobId,
            locationJob.actId
        ) as ArrayList<JobItemEstimateDTO>

        this@DeferredLocationViewModel.errorState = false
        var validProjectSectionId: String? = null
        var estimatIndex = -1


        locationJob.jobItemEstimates.forEachIndexed estimate@{ estIndex, uncheckedEstimate ->
            uncheckedEstimate.jobItemEstimatePhotos.forEachIndexed photo@{ phIndex, uncheckedPhoto ->
                if (locationJob.sectionId.isNullOrEmpty()){
                    val projectSectionId = jobCreationDataRepository.getProjectSectionIdForJobId(locationJob.jobId)
//                    val prjSction = jobCreationDataRepository.getProjectSectionForId(projectSectionId)
//                    prjSction.section
                    val locationQuery = LocationValidation(
                        projectId = locationJob.projectId!!,
                        jobId = locationJob.jobId,
                        estimateId = uncheckedEstimate.estimateId,
                        userId = locationJob.userId.toString(),
                        longitude = uncheckedPhoto.photoLongitude!!.round(8),
                        latitude = uncheckedPhoto.photoLatitude!!.round(8),
                        sectionId =  projectSectionId
                    )
                    val routeSectionResponse = getRouteSectionPoint(
                        locationQuery
                    )

                    when (routeSectionResponse) {

                        is XIResult.Error -> {
                            processRouteSectionError(
                                routeSectionResponse,
                                uncheckedPhoto,
                                uncheckedEstimate,
                                locationJob,
                                estIndex,
                                phIndex
                            )
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
                                handleLocationError(projectSectionIdResponse)
                            } else if (projectSectionIdResponse is XIResult.Success &&
                                validProjectSectionId.isNullOrBlank()
                            ) {
                                validProjectSectionId = projectSectionIdResponse.data.projectSectionId!!

                            }
                        }
                        else -> {
                            Timber.d("^*^ $routeSectionResponse")
                        }
                    }

                }else{
                    val locationQuery = LocationValidation(
                        projectId = locationJob.projectId!!,
                        jobId = locationJob.jobId,
                        estimateId = uncheckedEstimate.estimateId,
                        userId = locationJob.userId.toString(),
                        longitude = uncheckedPhoto.photoLongitude!!.round(8),
                        latitude = uncheckedPhoto.photoLatitude!!.round(8),
                        sectionId =  locationJob.sectionId
                    )
                    val routeSectionResponse = getRouteSectionPoint(
                        locationQuery
                    )

                    when (routeSectionResponse) {

                        is XIResult.Error -> {
                            processRouteSectionError(
                                routeSectionResponse,
                                uncheckedPhoto,
                                uncheckedEstimate,
                                locationJob,
                                estIndex,
                                phIndex
                            )
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
                                handleLocationError(projectSectionIdResponse)
                            } else if (projectSectionIdResponse is XIResult.Success &&
                                validProjectSectionId.isNullOrBlank()
                            ) {
                                validProjectSectionId = projectSectionIdResponse.data.projectSectionId!!

                            }
                        }
                        else -> {
                            Timber.d("^*^ $routeSectionResponse")
                        }
                    }
                }

            }


            // All photos processed
            try {
                val geoCoded = uncheckedEstimate.arePhotosGeoCoded()
                val checkedEstimate = uncheckedEstimate.copy(geoCoded = geoCoded)
                locationJob.jobItemEstimates[estIndex] = checkedEstimate
                estimatIndex = estIndex
                jobCreationDataRepository.backupEstimate(checkedEstimate)
            } catch (e: Exception) {
                val message = "Could not save updated estimate: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(e, message)
                withContext(mainContext) {
                    geoCodingResult.value = XIEvent(XIResult.Error(e, message))
                }
            }
        }

        // jobItemEstimates processed
        try {
            if (!validProjectSectionId.isNullOrBlank() && locationJob.isGeoCoded()) {

                val updatedJobId = updateOrCreateJobSection(locationJob, validProjectSectionId!!)
                if (updatedJobId.isNullOrBlank()) {
                    val projectItemId =  locationJob.jobItemEstimates[estimatIndex].projectItemId
                    failLocationValidation("Failed to create job section" ,projectItemId!!)
                } else {
                    withContext(mainContext) {
                        geoCodingResult.value = XIEvent(XIResult.Success(updatedJobId))
                    }
                }
            } else {
                val projectItemId =  locationJob.jobItemEstimates[estimatIndex].projectItemId
                failLocationValidation(getApplication<MainApp>() .getString(R.string.location_general_failure), projectItemId!!)

            }
        } catch (e: Throwable) {
            val message = "Failed to save geocoded job: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(e, message)
            handleLocationError(XIResult.Error(e, message))
        }

    }


    private suspend fun processRouteSectionError(
        routeSectionResponse: XIResult.Error,
        uncheckedPhoto: JobItemEstimatesPhotoDTO,
        uncheckedEstimate: JobItemEstimateDTO,
        locationJob: JobDTO,
        estIndex: Int,
        phIndex: Int
    ) {
        processAndPersist(
            routeSectionResponse = routeSectionResponse,
            uncheckedPhoto = uncheckedPhoto,
            uncheckedEstimate = uncheckedEstimate,
            locationJob = locationJob,
            estIndex = estIndex,
            phIndex = phIndex
        )
//        if (routeSectionResponse.exception is LocationException) {
        if (routeSectionResponse.message.isEmpty()) {
            return // Process the next photo
        } else {
            pushNonLocationException(routeSectionResponse)
        }
    }

    private fun handleLocationError(validationException: XIResult.Error) {

        if (validationException.exception is LocationException) {
            return
        } else {
            pushNonLocationException(validationException)
        }
    }

    private fun pushNonLocationException(operationException: XIResult.Error) = viewModelScope.launch(mainContext) {
        Timber.e(operationException.exception, operationException.message)
        geoCodingResult.value = XIEvent(operationException)
    }

    private fun failLocationValidation(message: String, projectItemId: String) = viewModelScope.launch(mainContext) {
                val estimate = jobCreationDataRepository.getItemForID(projectItemId)
                if (estimate.contractVoId.isNullOrEmpty()){
                    val estimateItem = jobCreationDataRepository.getItemPrjForID(projectItemId)
                    geoCodingResult.value =
                        XIEvent(
                            XIResult.Error(
                                LocationException(
                                    message
                                ),
                                "Are not within your allocated road reserve. ","Images For ${estimateItem.itemCode}  ${estimateItem.descr}"
                                //"One or more images not within your allocated road reserve."
                            )
                        )
                }else{
                    val estimateItm = jobCreationDataRepository.getItemPrjVoForID(projectItemId)
                    geoCodingResult.value =
                        XIEvent(
                            XIResult.Error(
                                LocationException(
                                    message
                                ),
                                "Are not within your allocated road reserve. ","Images For ${estimateItm.itemCode}  ${estimateItm.descr}"
                                //"One or more images not within your allocated road reserve."
                            )
                        )
                }

    }

    private suspend fun processAndPersist(
        locationResult: XIResult<LocationValidation>,
        job: JobDTO,
        uncheckedEstimate: JobItemEstimateDTO,
        uncheckedPhoto: JobItemEstimatesPhotoDTO,
        estIndex: Int,
        phIndex: Int
    ) = withContext(ioContext) {
        try {
            processAndPersist(locationResult, uncheckedPhoto, uncheckedEstimate, job, estIndex, phIndex)
        } catch (e: Exception) {
            Timber.e(e, "Could not save local changes")
            withContext(mainContext) {
                val localDataException = LocalDataException(e.message ?: XIErrorHandler.UNKNOWN_ERROR)
                geoCodingResult.value =
                    XIEvent(
                        XIResult.Error(
                            exception = localDataException,
                            "Could not update local data."
                        )
                    )
            }
        }
    }

    private suspend fun processAndPersist(
        routeSectionResponse: XIResult<LocationValidation>,
        uncheckedPhoto: JobItemEstimatesPhotoDTO,
        uncheckedEstimate: JobItemEstimateDTO,
        locationJob: JobDTO,
        estIndex: Int,
        phIndex: Int
    ) = withContext(ioContext) {
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
                withContext(mainContext) {
                    Timber.d("^*^ Good: ${theResult.data}")
                }
            }
            is XIResult.Error -> {
                if (theResult.exception is LocationException) {
                    treatedPhoto = estimatePhoto.copy(sectionMarker = theResult.message)
                    treatedPhoto = jobCreationDataRepository.backupEstimatePhoto(treatedPhoto)
                }

                withContext(dispatchers.main()) {
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
    ): String? = withContext(ioContext) {
        try {
            var jobSectionId: String? = null
            jobCreationDataRepository.backupJob(job)
            val jobToUpdate = jobCreationDataRepository.getUpdatedJob(job.jobId)
            val projectSection = jobCreationDataRepository.getSection(projectSectionId)
            if (!jobCreationDataRepository
                    .checkIfJobSectionExistForJobAndProjectSection(
                        jobId = DataConversion.toLittleEndian(jobToUpdate.jobId),
                        projectSectionId = DataConversion.toLittleEndian(projectSection.sectionId)
                    )
            ) {
                val newJobSections = jobToUpdate.jobSections as MutableList<JobSectionDTO>
                val newJobSection = createJobSection(projectSection, jobToUpdate)
                newJobSections.add(newJobSection)
                jobToUpdate.jobSections = newJobSections as ArrayList<JobSectionDTO>
                jobSectionId = newJobSection.jobSectionId
            } else {
                val existingJobSection = jobCreationDataRepository.getJobSectionByJobId(jobId = jobToUpdate.jobId)
                existingJobSection?.let { jobSection ->
                    jobSectionId = jobSection.jobSectionId
                    if (jobToUpdate.jobSections.isNullOrEmpty()) {
                        jobToUpdate.jobSections = ArrayList()
                    }
                    if (jobToUpdate.jobSections.isEmpty() || !job.jobSections.contains(existingJobSection)) {
                        jobToUpdate.jobSections.add(existingJobSection)
                    }
                }
            }

            jobToUpdate.sectionId = jobSectionId!!
            jobToUpdate.startKm = projectSection.startKm
            jobToUpdate.endKm = projectSection.endKm

            val locationResult = jobCreationDataRepository.updateNewJob(
                newJobId = jobToUpdate.jobId,
                startKM = projectSection.startKm,
                endKM = projectSection.endKm,
                sectionId = jobSectionId!!,
                newJobItemEstimatesList = jobToUpdate.jobItemEstimates,
                jobItemSectionArrayList = jobToUpdate.jobSections
            )
            return@withContext handleResult(locationResult)
        } catch (e: Exception) {
            Timber.e(e, "Could not save updated job.")
            return@withContext null
        }
    }

    fun resetGeoCodingResult() {
        geoCodingResult = MutableLiveData()
    }

    private suspend fun handleResult(jobResult: XIResult<JobDTO>): String? = withContext(ioContext) {
        var result: String? = null

        when (jobResult) {
            is XIResult.Success -> {
                val updatedJob = jobResult.data
                if (updatedJob.sectionId != null && updatedJob.jobSections.size >= 1) {
                    withContext(mainContext) {
                        result = updatedJob.jobId
                    }
                }
            }
            is XIResult.Error -> {
                withContext(mainContext) {
                    geoCodingResult.value = XIEvent(jobResult)
                }
            }
            else -> {
                Timber.e("^*^ - 3rd Outcome")
                Timber.e("$jobResult")
            }
        }
        return@withContext result
    }

    private suspend fun createJobSection(
        localProjectSection: ProjectSectionDTO,
        localJob: JobDTO
    ) = withContext(ioContext) {
        val jobSectionId = SqlLitUtils.generateUuid()
        JobSectionDTO(
            jobSectionId = jobSectionId,
            projectSectionId = localProjectSection.sectionId,
            jobId = localJob.jobId,
            startKm = localProjectSection.startKm,
            endKm = localProjectSection.endKm,
            recordSynchStateId = 0,
            recordVersion = 0
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
        val routeMarker = "${this.route} ${this.section} ${this.direction}" // at ${pointLocation?.round(3)} km"
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
