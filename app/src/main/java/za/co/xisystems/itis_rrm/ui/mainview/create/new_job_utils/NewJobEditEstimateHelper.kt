// package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils
//
// import android.location.Location
// import android.os.Bundle
// import za.co.xisystems.itis_rrm.ToDelete._commons.utils.NetworkUtils
// import za.co.xisystems.itis_rrm.ToDelete.data_access_layer.model.Section
// import za.co.xisystems.itis_rrm.ToDelete.utils.DateUtil
// import za.co.xisystems.itis_rrm.data.localDB.entities.*
// import za.co.xisystems.itis_rrm.base.BaseFragment
// import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.EstimatePhotoFragment
// import za.co.xisystems.itis_rrm.utils.Coroutines
// import za.co.xisystems.itis_rrm.utils.SqlLitUtils
// import za.co.xisystems.itis_rrm.utils.toast
// import java.util.*
//
// // TODO deprecate this
// class NewJobEditEstimateHelper(estimatePhotoFragment: EstimatePhotoFragment?) : BaseFragment() {
//
//    private val networkUtils = NetworkUtils()
//    private val estimatePhotoFragment = estimatePhotoFragment
//    var isTester = false
//    private lateinit var jobArrayList: ArrayList<JobDTO>
//
//    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
//    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
//    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
//    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
//    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
//    private lateinit var  itemSections : ArrayList<ItemSectionDTO>
//    private  var jobItemPhoto : JobItemEstimatesPhotoDTO? = null
//
//
//    internal var useR: Int? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        itemSections = ArrayList<ItemSectionDTO>()
//        jobArrayList = ArrayList<JobDTO>()
// //        jobItemPhoto = JobItemEstimatesPhotoDTO
//        jobItemSectionArrayList = ArrayList<JobSectionDTO>()
//        jobItemMeasureArrayList = ArrayList<JobItemMeasureDTO>()
//        newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
//        newJobItemEstimatesPhotosList = ArrayList<JobItemEstimatesPhotoDTO>()
//        newJobItemEstimatesWorksList = ArrayList<JobEstimateWorksDTO>()
// //        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()
//    }
//
//    fun processPhotoEstimate(
//        currentLocation: Location?,
//        filename_path: Map<String, String>,
//        itemId_photoType: Map<String, String>
//    ) { // TODO fix isTesting code smell
//        processPhotoEstima(currentLocation, filename_path, itemId_photoType, true)
//    }
// //    job: JobDTO, item: ItemDTOTemp?
//
//    fun processPhotoEstima(currentLocation: Location?, filename_path: Map<String, String>, itemId_photoType: Map<String, String>, isTesting : Boolean) {
//
//        val isPhotoStart = itemId_photoType["type"] == "start"
//        val itemId = itemId_photoType["itemId"] // itemId is ProjectItemId in jobItemEstimate
// //        val itemId = item?.itemId
//        // create job estimate
//        var jobItemEstimate = job?.getJobEstimateByItemId(itemId)
//
//        if (jobItemEstimate == null) {
//
//            val itemEstimate = createItemEstimate(itemId, job, newJobItemEstimatesPhotosList, jobItemMeasureArrayList, newJobItemEstimatesWorksList,jobItemPhoto!!)
//
//
//            val newjob = createNewJob(itemId, jobArrayList, useR, newJobItemEstimatesList, jobItemMeasureArrayList, jobItemSectionArrayList)
//
//            if (newjob != null) { saveNewJob(newjob) }
//
//            var jobItemEstimatePhoto = if (isPhotoStart) jobItemEstimate?.getJobItemEstimatePhotoStart() else jobItemEstimate!!.getJobItemEstimatePhotoEnd()
//            if (jobItemEstimatePhoto == null) {
//                val itemEstimatePhotos = createItemEstimatePhoto(itemEstimate, filename_path, currentLocation,jobItemPhoto!!)
//                val RouteSection = createRouteSection(itemEstimate, filename_path, currentLocation,jobItemPhoto!!)
//            }
//
//
//        }
//
//
//    }
//
//
//
//
//
//
//    private fun createRouteSection(
//        itemEstimate: JobItemEstimateDTO,
//        filenamePath: Map<String, String>,
//        currentLocation: Location?,
//        jobItemPhoto: JobItemEstimatesPhotoDTO
//    ): JobSectionDTO {
//        val newEstimateSection = JobSectionDTO("",null,null,0.0,0.0,null,0, 0)
// //        newJobItemEstimatesPhotosList.add(newEstimatePhoto)
//        return newEstimateSection
//    }
//
//    private fun createItemEstimatePhoto(
//        itemEst: JobItemEstimateDTO,
//        filename_path: Map<String, String>,
//        currentLocation: Location?,
//        jobItemPhoto: JobItemEstimatesPhotoDTO
//    ): JobItemEstimatesPhotoDTO {
//
//        if (currentLocation == null) {
//            estimatePhotoFragment?.activity!!.toast("Please make sure that you have activated the location on your device.")
//        } else {
//
//            if (jobItemPhoto.isPhotoStart()) {
//                jobItemPhoto.photoLatitude = currentLocation.latitude
//                jobItemPhoto.photoLongitude = currentLocation.longitude
//                itemEst.setJobItemEstimatePhotoStart(jobItemPhoto)
//            } else {
//                jobItemPhoto.photoLatitudeEnd = currentLocation.latitude
//                jobItemPhoto.photoLongitudeEnd = currentLocation.longitude
//                itemEst.setJobItemEstimatePhotoEnd(jobItemPhoto)
//            }
// //            getRouteSectionPoint(
// //                currentLocation,
// //                jobItemEstimate,
// //                jobItemEstimatePhoto,
// //                itemId_photoType_tester,
// //                3
// //            )
//        }
//
//        val photoId: String = SqlLitUtils.generateUuid()
//        val newEstimatePhoto = JobItemEstimatesPhotoDTO(
//            "", itemEst.estimateId, filename_path["filename"]!!,DateUtil.DateToString(Date()), photoId, null, null,
//            0.0, 0.0,currentLocation!!.latitude,currentLocation.longitude,currentLocation.latitude,currentLocation.longitude,filename_path["path"]!!,itemEst,
//            0,0,jobItemPhoto.isPhotoStart(), null
//        )
//        newJobItemEstimatesPhotosList.add(newEstimatePhoto)
//        return newEstimatePhoto!!
//    }
//
//    private fun createItemEstimate(
//        itemId: String?,
//        job: JobDTO,
//        newJobItemPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
//        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
//        jobItemWorksList: ArrayList<JobEstimateWorksDTO>,
//        jobItemPhoto: JobItemEstimatesPhotoDTO
//    ): JobItemEstimateDTO {
//        val estimateId: String = SqlLitUtils.generateUuid()
//
//        val newEstimate = JobItemEstimateDTO(
//            0, estimateId, job?.JobId,0.0, jobItemWorksList, newJobItemPhotosList, jobItemMeasureArrayList,
//            job, itemId,null,0.0,0,0,""
//
//        )
//        newJobItemEstimatesList.add(newEstimate)
//        return newEstimate!!
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    private fun saveNewJob(newjob: JobDTO) {
//        Coroutines.main {
// //            createViewModel.saveNewJob(newjob)
//        }
//    }
//
//    private fun createNewJob(
//        itemId: String?,
//        jobArrayList: ArrayList<JobDTO>,
//        useR: Int?,
//        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
//        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
//        jobItemSectionArrayList: ArrayList<JobSectionDTO>
//    ): JobDTO {
//
//        val newJobId: String = SqlLitUtils.generateUuid()
// //            job.JobId = newJobId
//        val estimateId: String = SqlLitUtils.generateUuid()
//
//        val newJob = JobDTO(
//            newJobId, itemId, null,useR!!,0,null,null,
//            null,null,null,0.0,0.0,null,null,
//            0,0,null,null,null,newJobItemEstimatesList,
//            jobItemMeasureArrayList,jobItemSectionArrayList,null,0,null,null,
//            null,null,0,0,0,0,0,
//            0,0,0,0,null,0,
//            0,null,null,null,null,0,null
//        )
//
//        jobArrayList.add(newJob)
//        return newJob!!
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
// //
// //    private fun getRouteSectionPoint(
// //        location: Location,
// //        jobItemEstimate: JobItemEstimateDTO?,
// //        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?,
// //        itemId_photoType_tester: Map<String, String>,
// //        type: Int
// //    ) {
// //        getRouteSectionPoint(
// //            location.longitude,
// //            location.latitude,
// //            jobItemEstimate,
// //            jobItemEstimatePhoto,
// //            itemId_photoType_tester,
// //            type
// //        )
// //    }
//
// //    @Deprecated("")
// //    private fun getRouteSectionPoint(
// //        longitude: Double,
// //        latitude: Double,
// //        jobItemEstimate: JobItemEstimateDTO?,
// //        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?,
// //        itemId_photoType_tester: Map<String, String>,
// //        type: Int
// //    ) {
// //        showProgressDialog(getString(R.string.please_wait_coordinates))
// //        val userId: Int? = userId
// //        if (userId == null) toast("Error: UserId is null in " + javaClass.simpleName) else if (!networkUtils.isNetworkAvailable(
// //                activity
// //            )
// //        ) { // Client is Offline
// //            getCallResponse(
// //                null,
// //                longitude,
// //                latitude,
// //                jobItemEstimate,
// //                jobItemEstimatePhoto!!,
// //                itemId_photoType_tester,
// //                type
// //            )
// //            dismissProgressDialog()
// //        } else { // TODO this is a quick fix, apply design pattern later
// //            val interactor = HealthCheckInteractor()
// //            interactor.executeHealthCheck(
// //                username,
// //                object :
// //                    ResponseListener<HealthCheckResponse?> {
// //                    private fun processOffline() {
// //                        getCallResponse(
// //                            null,
// //                            longitude,
// //                            latitude,
// //                            jobItemEstimate,
// //                            jobItemEstimatePhoto,
// //                            itemId_photoType_tester,
// //                            type
// //                        )
// //                        dismissProgressDialog()
// //                    }
// //
// ////                    override fun onSuccess(response: HealthCheckResponse) {
// ////
// ////                    }
// //
// //                    override fun onFailure(throwable: Throwable) {
// //                        onError("Error: server offline")
// //                        Log.d("x-", "ping.throwable> $throwable")
// //                    }
// //
// //                    override fun onError(message: String) {
// //                        Log.d("x-", "ping.onError.message> $message")
// //                        // Handle Server offline
// //                        processOffline()
// //                        toast(message)
// //                    }
// //
// //                    override fun onSuccess(response: HealthCheckResponse?) { T   RouteSectionPointInteractor().execute(
// //                            userId,
// //                            longitude,
// //                            latitude,
// //                            object : ResponseListener<GetRouteSectionPointResponse?> {
// //                                override fun onSuccess(response: GetRouteSectionPointResponse) {
// //                                    processRouteSectionPoint(
// //                                        response,
// //                                        longitude,
// //                                        longitude,
// //                                        jobItemEstimate,
// //                                        jobItemEstimatePhoto,
// //                                        itemId_photoType_tester,
// //                                        type
// //                                    )
// //                                }
// //
// //                                override fun onFailure(throwable: Throwable) {
// //                                    dismissProgressDialog()
// //                                    processOffline()
// //                                }
// //
// //                                override fun onError(message: String) {
// //                                    toast("Error: $message")
// //                                    dismissProgressDialog()
// //                                    processOffline()
// //                                }
// //                            })
// //                    }
// //                })
// //        }
// //    }
// //
// //    private val userId: Int
// //        private get() = RegistrationRepository.getUserId(activity)
// //
// //    private val username: String
// //        private get() = RegistrationRepository.getUsername(activity)
// //
// //    private fun processRouteSectionPoint(
// //        response: GetRouteSectionPointResponse, longitude: Double,
// //        latitude: Double, jobItemEstimate: JobItemEstimate?,
// //        jobItemEstimatePhoto: JobItemEstimatePhoto?,
// //        itemId_phototype_tester: Map<String, String>, type: Int
// //    ) {
// //        try { // TODO fix legacy code
// //            val sectionIdString = "" + response.sectionId
// //            val section =
// //                jobDataController.getSectionByRouteSectionProject(
// //                    response.linearId,
// //                    sectionIdString, response.direction, job.projectId
// //                )
// //            //  Cant find section for the project
// //            if (section == null) {
// //                toast(R.string.no_section_for_project)
// //            } else { //  If we have placed the section id for the job
// //                if (job.sectionId != null) { //  If the Job Section is not the same for the next item
// //                    if (section.sectionId == job.sectionId) { //  Check the Point Location in between Section start and end of project
// //                        if (section.startKm <= response.pointLocation && response.pointLocation <= section.endKm) {
// //                            job.sectionId = section.sectionId
// //                            getCallResponse(
// //                                response,
// //                                longitude,
// //                                latitude,
// //                                jobItemEstimate,
// //                                jobItemEstimatePhoto,
// //                                itemId_phototype_tester,
// //                                type
// //                            )
// //                        } else {
// //                            showSectionOutOfBoundError(section)
// //                        }
// //                    } else {
// //                        toast(getString(R.string.photo_from_other_section))
// //                    }
// //                } else { //  Check the Point Location in between Section start and end of project
// //                    if (section.startKm <= response.pointLocation && response.pointLocation <= section.endKm) { //  Set the Section
// //                        job.sectionId = section.sectionId
// //                        getCallResponse(
// //                            response,
// //                            longitude,
// //                            latitude,
// //                            jobItemEstimate!!,
// //                            jobItemEstimatePhoto,
// //                            itemId_phototype_tester,
// //                            type
// //                        )
// //                    } else {
// //                        showSectionOutOfBoundError(section)
// //                    }
// //                }
// //            }
// //        } catch (e: Exception) {
// //            e.printStackTrace()
// //            toast(e.toString())
// //        } finally {
// //            dismissProgressDialog()
// //        }
// //    }
//
//    private fun showSectionOutOfBoundError(section: Section) {
//        toast(
//            "You are not between the start: " + section.startKm +
//                    " and end: " + section.endKm + " co-ordinates for the project."
//        )
//    }
//
//    private val job: JobDTO
//        private get() = estimatePhotoFragment?.job!!
//
// //    private fun getCallResponse(
// //        response: GetRouteSectionPointResponse?,
// //        longitude: Double,
// //        latitude: Double,
// //        jobItemEstimate: JobItemEstimateDTO?,
// //        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?,
// //        itemId_photoType_tester: Map<String, String>,
// //        type: Int
// //    ) {
// //        if (response != null) { //  Start Photo
// //            if (jobItemEstimatePhoto!!.isPhotoStart) {
// //                if (job.StartKm <= response.pointLocation) {
// //                    job.StartKm = response.pointLocation
// //                }
// //                jobItemEstimatePhoto.startKm = response.pointLocation
// //            } else {
// //                jobItemEstimatePhoto.endKm = response.pointLocation
// //                //  Also lets set the max Job Start and Max End
// //                if (job.endKm <= response.pointLocation) {
// //                    job.endKm = response.pointLocation
// //                }
// //                jobItemEstimatePhoto.startKm = response.pointLocation
// //                //  Also lets set the min Job Start KM
// //                jobItemEstimatePhoto.endKm = response.pointLocation
// //            }
// //        } else { // TODO handle offline
// //            toast(R.string.working_offline)
// //            Log.d("x-offline", activity.getString(R.string.working_offline))
// //            //stepLocationInstance.reRunWithEstimates(jobItemEstimate, jobItemEstimatePhoto, latitude, longitude, 0.0, itemId_photoType_tester);
// //        }
// //        job.addOrUpdateJobItemEstimate(jobItemEstimate!!)
// //        activity.onPhotoEstimateProcessed(jobItemEstimatePhoto!!.isPhotoStart)
// //        dismissProgressDialog()
// //    }
// //
// //    /**
// //     * checkOnline
// //     * Check to see when Online to Sync Items
// //     */
// //    fun updatePhotosLocation(): Boolean {
// //        var canProceed = true
// //        val hasActiveInternet = JobUtil.checkActiveInternetConnection(activity)
// //        if (hasActiveInternet) {
// //            for (estimate in job.jobItemEstimates) for (photo in estimate.jobItemEstimatePhotos) {
// //                if (job.sectionId == null) {
// //                    getRouteSectionPointSingleCall(
// //                        photo.photoLongitude,
// //                        photo.photoLatitude,
// //                        photo,
// //                        "start"
// //                    )
// //                    getRouteSectionPointSingleCall(
// //                        photo.photoLongitudeEnd,
// //                        photo.photoLatitudeEnd,
// //                        photo,
// //                        "end"
// //                    )
// //                    canProceed = false
// //                }
// //            }
// //        } else canProceed = false
// //        return canProceed
// //    }
// //
// //    private fun getRouteSectionPointSingleCall(
// //        longitude: Double, latitude: Double,
// //        jobItemEstimatePhoto: JobItemEstimatePhoto,
// //        type: String
// //    ) {
// //        Log.d("x-g", "getRouteSectionPointSingleCall")
// //        val model = StepLocationViewModel()
// //        val userid = RegistrationRepository.getUserId(activity)
// //        if (userid != null) {
// //            val messages =
// //                arrayOf("Fetching route section info...")
// //            // TODO fix progress view
// //            model.getRouteSectionPoint(
// //                userid,
// //                longitude,
// //                latitude,
// //                object :
// //                    BaseCallBackView<GetRouteSectionPointResponse?>(
// //                        activity,
// //                        messages
// //                    ) {
// //                    override fun processData(response: GetRouteSectionPointResponse?) {
// //                        processGetRouteSectionPointResponse(response, jobItemEstimatePhoto, type)
// //                    }
// //                })
// //        }
// //    }
// //
// //    private fun someErrorOccur() {
// //        toast("Error: Some error occur")
// //    }
// //
// //    private fun processGetRouteSectionPointResponse(
// //        response: GetRouteSectionPointResponse?,
// //        jobItemEstimatePhoto: JobItemEstimatePhoto,
// //        type: String
// //    ) {
// //        Log.d("x-x", "processGetRouteSectionPointResponse $type")
// //        try {
// //            if (response!!.isSuccess) { //  Get Section Id for the Project
// //                val section =
// //                    jobDataController.getSectionByRouteSectionProject(
// //                        response.linearId,
// //                        response.sectionId.toString(),
// //                        response.direction,
// //                        job.projectId
// //                    )
// //                //  Cant find section for the project
// //                if (section == null) { //  Get Which Item we cannot find the section due to co-ordinates
// //                    val estimate =
// //                        jobDataController.getJobItemEstimateForEstimateId(jobItemEstimatePhoto.estimateId)
// //                    if (estimate == null) someErrorOccur()
// //                    val item =
// //                        jobDataController.getItemForItemId(estimate!!.projectItemId)
// //                    Toast.makeText(
// //                        activity,
// //                        activity.getString(R.string.no_section_for_project_item).toString() + item.descr + " please make" +
// //                                " sure the co-ordinates are correct for the item.",
// //                        Toast.LENGTH_LONG
// //                    ).show()
// //                    activity.dismissProgressDialog()
// //                } else { //  If we have placed the section id for the job
// //                    if (job.sectionId != null) { //  If the Job Section is not the same for the next item
// //                        if (section.sectionId == job.sectionId) { //  Check the Point Location in between Section start and end of project
// //                            if (response.pointLocation >= section.startKm && response.pointLocation <= section.endKm) {
// //                                updateSections(response, jobItemEstimatePhoto, type)
// //                                //  Refresh
// //                                activity.onPhotoEstimateProcessed(jobItemEstimatePhoto.isPhotoStart)
// //                            } else {
// //                                Toast.makeText(
// //                                    activity,
// //                                    "You are not between the start: " + section.startKm +
// //                                            " and end: " + section.endKm + " co-ordinates for the project.",
// //                                    Toast.LENGTH_LONG
// //                                ).show()
// //                            }
// //                        } else { //  Get Which Item we cannot find the section due to co-ordinates
// //                            val estimate =
// //                                jobDataController.getJobItemEstimateForEstimateId(
// //                                    jobItemEstimatePhoto.estimateId
// //                                )
// //                            if (estimate == null) someErrorOccur() else {
// //                                val item =
// //                                    jobDataController.getItemForItemId(estimate.projectItemId)
// //                                Toast.makeText(
// //                                    activity,
// //                                    "The Item:" + item.descr + " " +
// //                                            activity.getString(R.string.photo_from_other_section_item),
// //                                    Toast.LENGTH_LONG
// //                                ).show()
// //                            }
// //                        }
// //                    } else { //  Check the Point Location in between Section start and end of project
// //                        if (response.pointLocation >= section.startKm && response.pointLocation <= section.endKm) { //  Set the Section
// //                            job.sectionId = section.sectionId
// //                            updateSections(response, jobItemEstimatePhoto, type)
// //                            //  Refresh
// //                            activity.onPhotoEstimateProcessed(jobItemEstimatePhoto.isPhotoStart)
// //                        } else {
// //                            Toast.makeText(
// //                                activity,
// //                                "You are not between the start: " + section.startKm +
// //                                        " and end: " + section.endKm + " co-ordinates for the project.",
// //                                Toast.LENGTH_LONG
// //                            ).show()
// //                        }
// //                    }
// //                    activity.dismissProgressDialog()
// //                }
// //            } else {
// //                activity.dismissProgressDialog()
// //                Toast.makeText(activity, response.getErrorMessage(), Toast.LENGTH_LONG).show()
// //            }
// //        } catch (e: Exception) {
// //            activity.dismissProgressDialog()
// //            e.printStackTrace()
// //        }
// //    }
// //
// //    fun updateSections(
// //        response: GetRouteSectionPointResponse?,
// //        jobItemEstimatePhoto: JobItemEstimatePhoto,
// //        type: String
// //    ) {
// //        if (type === "start") {
// //            if (job.StartKm < response!!.pointLocation) {
// //                job.StartKm = response.pointLocation
// //            }
// //            jobItemEstimatePhoto.startKm = response.pointLocation
// //        }
// //        if (type === "end") {
// //            if (job.EndKm < response!!.pointLocation) {
// //                job.EndKm = response.pointLocation
// //            }
// //            jobItemEstimatePhoto.endKm = response.pointLocation
// //        }
// //        //  Lets Update the Job
// //        jobDataController.updateJobRecord(job)
// //        jobDataController.updateJobItemEstimatePhoto(jobItemEstimatePhoto)
// //    }
// //
// //    init {
// //        jobDataController =
// //            JobDataController(estimatePhotoFragment)
// //    }
// }
