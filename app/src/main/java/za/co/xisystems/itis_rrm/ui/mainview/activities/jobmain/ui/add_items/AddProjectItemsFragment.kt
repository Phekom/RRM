package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.HandlerCompat
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MobileNavigationDirections
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants.ONE_SECOND
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentAddItemsBinding
import za.co.xisystems.itis_rrm.databinding.NewJobItemBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.DeferredLocationViewModel
import za.co.xisystems.itis_rrm.services.DeferredLocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.extensions.*
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModel
import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModelFactory
import java.util.*
import kotlin.collections.ArrayList

class AddProjectItemsFragment : BaseFragment() {

    private lateinit var addViewModel: AddItemsViewModel
    private val factory: AddItemsViewModelFactory by instance()
    private var _binding: FragmentAddItemsBinding? = null
    private val binding get() = _binding!!
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private lateinit var deferredLocationViewModel: DeferredLocationViewModel
    private val unsubFactory: UnSubmittedViewModelFactory by instance()
    private val deferredLocationFactory: DeferredLocationViewModelFactory by instance()
    private lateinit var viewModel: ImagePickerViewModel
    private val captureFactory: ImagePickerViewModelFactory by instance()
    private var dueDateDialog: DatePickerDialog? = null
    private var startDateDialog: DatePickerDialog? = null
    private lateinit var jobDataController: JobDataController
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder<NewJobItemBinding>>
    private lateinit var startDate: Date
    private lateinit var dueDate: Date
    private val photoUtil: PhotoUtil by instance()
    private var jobId: String? = null
    private var projectId: String? = null
    private var stateRestored: Boolean = false
    private lateinit var job: JobDTO
    private var items: List<ItemDTOTemp> = ArrayList()
    private var jobBound: Boolean = false
    private val projectArgsData: AddProjectItemsFragmentArgs by navArgs()

    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>


    private val backClickListener = View.OnClickListener {
        setBackPressed(it)
    }
    private lateinit var userId: String

    companion object {
        private const val JOB_KEY = "jobId"
        private const val PROJECT_KEY = "projectId"
    }

    init {
        lifecycleScope.launch {
            whenCreated {
                initViewModels()
            }
            whenStarted {
                requireActivity().hideKeyboard()
                uiUpdate()
            }
            whenResumed {
                if (!this@AddProjectItemsFragment::job.isInitialized) {
                    uiUpdate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        groupAdapter = GroupAdapter<GroupieViewHolder<NewJobItemBinding>>()
        jobDataController = JobDataController

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navToCreate(this@AddProjectItemsFragment.requireView())
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this@AddProjectItemsFragment, callback)
    }

    private fun navToCreate(view: View) {
        val directions = AddProjectItemsFragmentDirections.actionNavigationAddItemsToNavigationCreate()
        Navigation.findNavController(view).navigate(directions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemsBinding.inflate(inflater, container, false)
        _binding?.toolbar?.apply {
            setOnBackClickListener(backClickListener)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModels()
        Coroutines.main {

            job = addViewModel.getJobForId(projectArgsData.jobId!!)
            _binding?.toolbar?.setTitle(job.descr)
            val contractNo =
                addViewModel.getContractNoForId(job.contractVoId)
            val projectCode =
                addViewModel.getProjectCodeForId(job.projectId)
            binding.selectedContractTextView.text = contractNo
            binding.selectedProjectTextView.text = projectCode

            when {
                !projectArgsData.jobId.isNullOrBlank() -> {
                    onRestoreInstanceState(projectArgsData.toBundle())
                }
                savedInstanceState != null && !stateRestored -> {
                    onRestoreInstanceState(savedInstanceState)
                }

                !stateRestored -> {
                    newJobItemEstimatesList = ArrayList()
                    // Generic new job layout
                    binding.lastLin.visibility = View.GONE
                    binding.totalCostTextView.visibility = View.GONE
                    binding.dueDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
                    binding.startDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
                    startDate = DateUtil.currentDateTime!!
                    dueDate = DateUtil.currentDateTime!!
                }
            }


            setmyClickListener()
            bindProjectItems(job)
        }

    }

    private fun initViewModels() {
        addViewModel =
            ViewModelProvider(this, factory)[AddItemsViewModel::class.java]

        unsubmittedViewModel =
            ViewModelProvider(this.requireActivity(), unsubFactory)[UnSubmittedViewModel::class.java]

        deferredLocationViewModel =
            ViewModelProvider(this.requireActivity(), deferredLocationFactory)[DeferredLocationViewModel::class.java]

        viewModel =
            ViewModelProvider(this.requireActivity(), captureFactory)[ImagePickerViewModel::class.java]
    }

    private fun bindProjectItems(job: JobDTO) = uiScope.launch(uiScope.coroutineContext) {
        val projectItems = addViewModel.getAllProjectItems(job.projectId!!, job.jobId)
        projectItems.observe(viewLifecycleOwner, { itemList ->
            when {
                itemList.isEmpty() -> {
                    clearProjectItems()
                }
                else -> {
                    populateProjectItemView(itemList)
                }
            }
        })
    }


    fun uiUpdate() {
        jobBound = false
        uiScope.launch(uiScope.coroutineContext) {
            bindCosting()
            initCurrentUserObserver()
            initCurrentJobListener()
            initValidationListener()
        }
    }

    private fun initCurrentUserObserver() {
        addViewModel.loggedUser.observe(viewLifecycleOwner, { userId ->
            userId?.let {
                this@AddProjectItemsFragment.userId = it.toString()
            }
        })
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.run {
            jobId = getString(JOB_KEY)
            projectId = getString(PROJECT_KEY)
        }

        jobId?.let { restoredId ->
            addViewModel.setJobToEdit(restoredId)
            stateRestored = true
        }
    }


    private fun populateProjectItemView(itemList: List<ItemDTOTemp>) {
        items = itemList.filter { itemDTOTemp ->
            itemDTOTemp.jobId == job.jobId
        }
        when {
            items.isNotEmpty() -> {
                initRecyclerView(items.toProjecListItems())
            }
            else -> {
                clearProjectItems()
            }
        }
    }

    private fun initRecyclerView(projectListItems: List<ProjectItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<NewJobItemBinding>>().apply {
            _binding?.infoTextView?.visibility = View.GONE
            update(projectListItems)
        }

        _binding?.projectRecyclerView?.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<ItemDTOTemp>.toProjecListItems(): List<ProjectItem> {
        return this.map {
            ProjectItem(
                fragment = this@AddProjectItemsFragment,
                tempItem = it,
                addViewModel = addViewModel,
                contractID = job.contractVoId,
                job = job
            )
        }
    }

    private fun clearProjectItems() {
        groupAdapter.clear()
        binding.totalCostTextView.text = ""
        binding.lastLin.visibility = View.GONE
        binding.totalCostTextView.visibility = View.GONE
        binding.submitButton.visibility = View.GONE
        binding.infoTextView.visibility = View.VISIBLE
    }


    private fun setmyClickListener() {
        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.addItemButton -> {
                    openSelectItemFragment(view)
                }

                R.id.resetButton -> {
                    onResetClicked(view)
                }

                R.id.infoTextView -> {
                    if (view.visibility == View.VISIBLE) {
                        openSelectItemFragment(view)
                    }
                }

                R.id.startDateCardView -> {
                    selectStartDate()
                }

                R.id.dueDateCardView -> {
                    selectDueDate()
                }

                R.id.submitButton -> {
                    Coroutines.main {
                        if (this@AddProjectItemsFragment.requireContext().isConnected) {
                            executeWorkflow()
                        } else {
                            displayConnectionWarning()
                        }
                    }
                }
            }
        }

        _binding?.addItemButton?.setOnClickListener(myClickListener)
        _binding?.resetButton?.setOnClickListener(myClickListener)
        _binding?.startDateCardView?.setOnClickListener(myClickListener)
        _binding?.dueDateCardView?.setOnClickListener(myClickListener)
        _binding?.submitButton?.setOnClickListener(myClickListener)
        _binding?.infoTextView?.setOnClickListener(myClickListener)
    }

    private fun openSelectItemFragment(view: View) {
        val directions = AddProjectItemsFragmentDirections.actionNavigationAddItemsToNavigationSelectItems(job.projectId!!, job.jobId)
        Navigation.findNavController(view).navigate(directions)
//        Coroutines.io {
//            withContext(Dispatchers.Main.immediate) {
//                val navDirection = AddProjectItemsFragmentDirections
//                    .actionNavigationAddItemsToNavigationSelectItems(job.projectId!!, job.jobId)
//                Navigation.findNavController(view).navigate(navDirection)
//
//            }
//        }

    }

    private suspend fun executeWorkflow() {
        when (job.actId) {
            ActivityIdConstants.JOB_PENDING_UPLOAD -> {
                addViewModel.resetUploadState()
                initReUploadListener()
                addViewModel.setJobForReUpload(job.jobId)
                toggleLongRunning(true)
                _binding?.submitButton?.initProgress(viewLifecycleOwner)
                _binding?.submitButton?.startProgress("Uploading job ...")
            }
            else -> {
                resetValidationListener()
                validateJob(job)
            }
        }
    }

    private fun displayConnectionWarning() {
        this@AddProjectItemsFragment.extensionToast(
            title = "No Connectivity",
            message = "Job validation and submission requires an active internet connection.",
            style = ToastStyle.NO_INTERNET,
            duration = ToastDuration.LONG,
            position = ToastGravity.BOTTOM
        )
    }


    private fun resetValidationListener() {
        deferredLocationViewModel.geoCodingResult.removeObservers(viewLifecycleOwner)
        deferredLocationViewModel.resetGeoCodingResult()
        addViewModel.jobForValidation.removeObservers(viewLifecycleOwner)
        addViewModel.jobForSubmission.removeObservers(viewLifecycleOwner)
        addViewModel.resetValidationState()
        Coroutines.main {
            initValidationListener()
        }
    }

    private fun initReUploadListener() {
        addViewModel.jobForReUpload.observeOnce(viewLifecycleOwner, { reUploadEvent ->
            reUploadEvent.getContentIfNotHandled()?.let { reUploadJob ->
                Coroutines.main {
                    val result = addViewModel.reUploadJob(
                        job = reUploadJob,
                        activity = this@AddProjectItemsFragment.requireActivity()
                    )
                    handleUploadResult(result)
                }
            }
        })
    }

    private fun handleUploadResult(result: XIResult<Boolean>) {
        toggleLongRunning(false)
        when (result) {
            is XIResult.Success -> {
                _binding?.submitButton?.doneProgress("Uploaded!")
                extensionToast(
                    message = "Job: ${job.descr} uploaded successfully",
                    style = ToastStyle.SUCCESS
                )
                addViewModel.deleteItemList(job.jobId)
                addViewModel.deleteJobFromList(job.jobId)
                toggleLongRunning(false)
                Navigation.findNavController(this.requireView()).popBackStack(R.id.nav_unSubmitted, false)
            }
            is XIResult.Error -> {
                extensionToast(
                    title = "Upload failed.",
                    message = "${result.message} - please retry again later.",
                    style = ToastStyle.ERROR
                )
                _binding?.submitButton?.failProgress("Retry Upload?")
                addViewModel.resetUploadState()
            }
            else -> {
                Timber.e("$result")
            }
        }
    }

    private fun onResetClicked(view: View) {
        Coroutines.main {
            addViewModel.deleteItemList(job.jobId)
            addViewModel.deleteJobFromList(job.jobId)

        }
        resetContractAndProjectSelection(view)
    }

    private fun resetContractAndProjectSelection(view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = AddProjectItemsFragmentDirections
                    .actionNavigationAddItemsToNavigationCreate()
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    private fun setBackPressed(view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = AddProjectItemsFragmentDirections
                    .actionNavigationAddItemsToNavigationCreate()
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }


    private fun selectDueDate() {
        _binding?.dueDateCardView?.startAnimation(click) // Get Current Date
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        dueDateDialog = activity?.let {
            DatePickerDialog(
                it,
                { _, year, month, dayOfMonth ->
                    setDueDateTextView(year, month, dayOfMonth)
                    dueDate = Date.from(c.toInstant())
                }, year, month, day
            )
        }
        dueDateDialog!!.datePicker.minDate = System.currentTimeMillis() - ONE_SECOND
        dueDateDialog!!.show()
    }

    private fun selectStartDate() {
        _binding?.startDateCardView?.startAnimation(click)
        // Get Current Date
        val startDateCalender = Calendar.getInstance()
        val startYear = startDateCalender[Calendar.YEAR]
        val startMonth = startDateCalender[Calendar.MONTH]
        val startDay = startDateCalender[Calendar.DAY_OF_MONTH]
        startDateDialog = activity?.let {
            DatePickerDialog(
                it,
                { _, year, month, dayOfMonth ->
                    setStartDateTextView(year, month, dayOfMonth)
                }, startYear, startMonth, startDay
            )
        }
        startDateDialog!!.datePicker.minDate = System.currentTimeMillis() - ONE_SECOND
        startDateDialog!!.show()
    }


    private fun popViewOnJobSubmit() {
        // Delete Items data from the database after success upload
        // onResetClicked(this.requireView())
        // addViewModel.setJobToEdit("null")
        // Conduct user back to home fragment
        //            val direction = MobileNavigationDirections.actionGlobalNavHome()
//            Navigation.findNavController(this@AddProjectItemsFragment.requireView())
//                .navigate(direction)
        HandlerCompat.postDelayed(Handler(Looper.getMainLooper()), {
            Intent(requireContext(), MainActivity::class.java).also { home ->
                home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(home)
            }
        }, null, TWO_SECONDS)
    }

    private fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        _binding?.dueDateTextView?.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        _binding?.dueDateCardView?.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job.dueDate = DateUtil.dateToString(Date.from(calendar.toInstant()))
        backupJobInProgress(job)
    }


    private fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        _binding?.startDateTextView?.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        _binding?.startDateCardView?.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job.startDate = DateUtil.dateToString(Date.from(calendar.toInstant()))
        backupJobInProgress(job)
    }

    private fun backupJobInProgress(jobDTO: JobDTO?) {
        Coroutines.io {
            addViewModel.backupJob(jobDTO!!)
        }
    }


    private fun onGeoLocationFailed() {
        Coroutines.ui {
            toggleLongRunning(false)
            HandlerCompat.postDelayed(Handler(Looper.getMainLooper()), {
                addViewModel.setJobToEdit(projectArgsData.jobId!!)
                initCurrentJobListener()
            }, null, TWO_SECONDS)
        }
    }

    private fun validateCalendar(): Boolean {

        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1) // number represents number of days
        val yesterday = cal.time

        var startResult = false
        var dueResult = false

        if (job.dueDate != null) {

            if (dueDate < startDate || dueDate < yesterday || dueDate == yesterday
            ) {
                extensionToast(message = "Please select a valid due date", style = ToastStyle.WARNING)
                _binding?.dueDateCardView?.startAnimation(shake_long)
            } else {
                job.dueDate
                dueResult = true
            }
        } else {
            extensionToast(message = "Please select a Due Date", style = ToastStyle.WARNING)
            _binding?.dueDateCardView?.startAnimation(shake_long)
        }
        if (job.startDate != null) {
            if (startDate < yesterday || dueDate == yesterday
            ) {
                extensionToast(message = "Please select a valid Start Date", style = ToastStyle.WARNING)
                _binding?.dueDateCardView?.startAnimation(shake_long)
            } else {
                job.startDate
                startResult = true
            }
        } else {
            extensionToast(message = "Please select Start Date", style = ToastStyle.WARNING)
            _binding?.startDateCardView?.startAnimation(shake_long)
        }

        return startResult && dueResult
    }


    private suspend fun validateEstimates(updatedJob: JobDTO) = withContext(dispatchers.io()) {
        val validated = addViewModel.areEstimatesValid(updatedJob, ArrayList(items))

        withContext(dispatchers.ui()) {

            if (!validated) {
                onInvalidJob()
            } else {
                updatedJob.issueDate = DateUtil.dateToString(Date())
                addViewModel.backupJob(updatedJob)
                addViewModel.setJobForSubmission(updatedJob.jobId)
            }
        }
    }


    private fun onInvalidJob() {
        toggleLongRunning(false)
        _binding?.submitButton?.failProgress("Submit")
        extensionToast(message = "Incomplete estimates!", style = ToastStyle.ERROR)
        _binding?.itemsCardView?.startAnimation(shake_long)
    }


    private suspend fun initValidationListener() = withContext(dispatchers.main()) {
        deferredLocationViewModel.geoCodingResult.observeOnce(
            viewLifecycleOwner, { result ->
                result.getContentIfNotHandled()?.let { outcome ->
                    uiScope.launch {
                        processLocationResult(outcome)
                    }
                }
            }
        )

        addViewModel.jobForValidation.observeOnce(
            viewLifecycleOwner, { job ->
                job.getContentIfNotHandled()?.let { realJob ->
                    if (!realJob.sectionId.isNullOrBlank() && JobUtils.isGeoCoded(realJob)) {
                        uiScope.launch {
                            validateEstimates(realJob)
                        }
                    } else {
                        uiScope.launch {
                            geoLocationFailed()
                        }
                    }
                }
            }
        )

        addViewModel.jobForSubmission.observeOnce(
            viewLifecycleOwner, { unsubmittedEvent ->
                unsubmittedEvent.getContentIfNotHandled()?.let { submissionJob ->
                    uiScope.launch {
                        addViewModel.backupSubmissionJob.value = XIEvent(submissionJob)
                        submitJob(submissionJob)
                    }
                }
            }
        )
    }


    private suspend fun processLocationResult(result: XIResult<String>) = withContext(dispatchers.main()) {
        when (result) {
            is XIResult.Success -> {
                handleGeoSuccess(result)
            }
            is XIResult.Error -> {
                handleGeoError(result)
            }
            else -> {
                Timber.d("$result")
            }
        }
    }

    private suspend fun handleGeoError(
        result: XIResult.Error
    ) = withContext(dispatchers.main()) {
        this@AddProjectItemsFragment.extensionToast(
            title = "Location Validation",
            message = result.exception.message.toString(),
            style = ToastStyle.ERROR
        )
        crashGuard(
            throwable = result,
            refreshAction = { Coroutines.main { this@AddProjectItemsFragment.validateJob(job) } }
        )
        geoLocationFailed()
    }


    private fun handleGeoSuccess(result: XIResult.Success<String>) {
        val geoCodedJobId = result.data
        addViewModel.setJobToValidate(geoCodedJobId)
    }

    private suspend fun geoLocationFailed() = withContext(Dispatchers.Main.immediate) {
        _binding?.submitButton?.failProgress("Locations not verified ...")
        toggleLongRunning(false)
        _binding?.itemsCardView?.startAnimation(shake_long)
        onGeoLocationFailed()
    }

    private suspend fun validateJob(invalidJob: JobDTO) = uiScope.launch(uiScope.coroutineContext) {

        if (invalidJob.jobId.isNotEmpty() && validateCalendar()) {

            withContext(dispatchers.main()) {
                if (!JobUtils.areQuantitiesValid(invalidJob)) {
                    this@AddProjectItemsFragment.extensionToast(
                        message = "Error: incomplete estimates.\n Quantity can't be zero!",
                        style = ToastStyle.WARNING
                    )
                    _binding?.itemsCardView?.startAnimation(shake_long)
                } else {
                    if (!invalidJob.isGeoCoded()) {
                        validateLocations(invalidJob)
                    } else {
                        addViewModel.setJobToValidate(invalidJob.jobId)
                    }
                }
            }
        }
    }

    private suspend fun validateLocations(job: JobDTO) = withContext(dispatchers.io()) {
        addViewModel.backupJob(job)
        withContext(dispatchers.ui()) {
            toggleLongRunning(true)
            _binding?.submitButton?.initProgress(viewLifecycleOwner)
            _binding?.submitButton?.startProgress("Checking locations ...")
        }
        deferredLocationViewModel.checkLocations(job.jobId)
    }


    private suspend fun submitJob(
        job: JobDTO
    ) = withContext(uiScope.coroutineContext) {
        toggleLongRunning(true)
        _binding?.submitButton?.initProgress(viewLifecycleOwner)
        _binding?.submitButton?.startProgress("Submitting Job ...")
        val jobTemp = jobDataController.setJobLittleEndianGuids(job)
        saveRrmJob(job.userId, jobTemp)
    }


    private suspend fun saveRrmJob(
        userId: Int,
        job: JobDTO
    ) = withContext(uiScope.coroutineContext) {
        val result = addViewModel.submitJob(userId, job, requireActivity())
        processSubmission(result)
    }

    private fun initCurrentJobListener() {
        addViewModel.jobToEdit.distinctUntilChanged().observe(
            viewLifecycleOwner, { jobRecord ->
                jobRecord?.let { jobToEdit ->
                    job = jobToEdit
                    jobId = job.jobId
                    jobBound = true
                    Coroutines.main {

                        val contractNo =
                            addViewModel.getContractNoForId(job.contractVoId)
                        val projectCode =
                            addViewModel.getProjectCodeForId(job.projectId)
                        _binding?.selectedContractTextView?.text = contractNo
                        _binding?.selectedProjectTextView?.text = projectCode

                        _binding?.infoTextView?.visibility = View.GONE
                        _binding?.lastLin?.visibility = View.VISIBLE
                        _binding?.totalCostTextView?.visibility = View.VISIBLE

                        // Set Job Start & Completion Dates
                        job.dueDate?.let {
                            _binding?.dueDateTextView?.text = DateUtil.toStringReadable(DateUtil.stringToDate(it))
                            dueDate = DateUtil.stringToDate(it)!!
                        }

                        job.startDate?.let {
                            _binding?.startDateTextView?.text = DateUtil.toStringReadable(DateUtil.stringToDate(it))
                            startDate = DateUtil.stringToDate(it)!!
                        }

                        addViewModel.tempProjectItem.observe(viewLifecycleOwner, {
                            it.getContentIfNotHandled()?.let {
                                _binding?.infoTextView?.visibility = View.GONE
                                _binding?.lastLin?.visibility = View.VISIBLE
                                _binding?.totalCostTextView?.visibility = View.VISIBLE
                            }
                        })
                        bindProjectItems()
                        if (job.actId == 1) {
                            _binding?.addItemButton?.visibility = View.INVISIBLE
                            _binding?.submitButton?.text = getString(R.string.complete_upload)
                        }
                    }
                }
            }
        )
    }


    private fun bindProjectItems() = uiScope.launch(uiScope.coroutineContext) {
        val projectItems =
            addViewModel.getAllProjectItems(job.projectId!!, job.jobId)
        projectItems.observe(viewLifecycleOwner, { itemList ->
            when {
                itemList.isEmpty() -> {
                    clearProjectItems()
                }
                else -> {
                    populateProjectItemView(itemList)
                }
            }
        })
    }


    private suspend fun processSubmission(result: XIResult<Boolean>) {
        when (result) {
            is XIResult.Success -> {
                toggleLongRunning(false)
                withContext(Dispatchers.Main.immediate) {
                    addViewModel.eraseUsedAndExpiredPhotos(job)
                    val images = viewModel.fetchImagesFromExternalStorage()
                    job.jobItemEstimates.forEach { estimate ->
                        estimate.jobItemEstimatePhotos.forEach { image ->
                            images.forEach {
                                if (image.filename == it.name) {
                                    deleteFileFromUri(requireContext(), it.uri)
                                }
                            }
                            photoUtil.deleteImageFile(image.photoPath)
                        }
                    }

                    this@AddProjectItemsFragment.requireActivity().extensionToast(
                        message = getString(R.string.job_submitted),
                        style = ToastStyle.SUCCESS
                    )
                }
                popViewOnJobSubmit()
            }
            is XIResult.Error -> {
                withContext(Dispatchers.Main.immediate) {
                    toggleLongRunning(false)
                    _binding?.submitButton?.failProgress("Submission Failed")
                    crashGuard(
                        throwable = result,
                        refreshAction = { this@AddProjectItemsFragment.resubmitJob() }
                    )
                }
            }
            else -> {
                Timber.d("x -> $result")
            }
        }
    }

    private fun deleteFileFromUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }


    private fun bindCosting() = uiScope.launch(dispatchers.ui()) {
        addViewModel.totalJobCost.observe(viewLifecycleOwner, { costingRecord ->
            costingRecord?.let {
                _binding?.totalCostTextView?.text = it
            }
        })
    }


    private fun resubmitJob() {
        IndefiniteSnackbar.hide()
        addViewModel.backupSubmissionJob.observeOnce(viewLifecycleOwner, { retryJobSubmission ->
            retryJobSubmission.getContentIfNotHandled()?.let { repeatJob ->
                uiScope.launch(uiScope.coroutineContext) {
                    resetValidationListener()
                    validateJob(repeatJob)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}