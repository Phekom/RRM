/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 07:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.os.HandlerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_add_project_items.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants.ONE_SECOND
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.WARNING
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentAddProjectItemsBinding
import za.co.xisystems.itis_rrm.databinding.NewJobItemBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.DeferredLocationViewModel
import za.co.xisystems.itis_rrm.services.DeferredLocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SwipeTouchCallback
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.Calendar
import java.util.Date

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class AddProjectFragment : BaseFragment(), DIAware {

    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private lateinit var deferredLocationViewModel: DeferredLocationViewModel
    private val unsubFactory: UnSubmittedViewModelFactory by instance()
    private val createFactory: CreateViewModelFactory by instance()
    private val deferredLocationFactory: DeferredLocationViewModelFactory by instance()
    private var dueDateDialog: DatePickerDialog? = null
    private var startDateDialog: DatePickerDialog? = null
    private lateinit var jobDataController: JobDataController
    private val swipeSection = Section()
    private var contractID: String? = null
    private var projectID: String? = null
    private lateinit var userId: String
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder<NewJobItemBinding>>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var startDate: Date
    private lateinit var dueDate: Date
    private var _ui: FragmentAddProjectItemsBinding? = null
    private val ui get() = _ui!!
    private var uiScope = UiLifecycleScope()
    private lateinit var job: JobDTO
    private var items: List<ItemDTOTemp> = ArrayList()
    private var stateRestored: Boolean = false
    private var jobBound: Boolean = false
    val addProjectArgs: AddProjectFragmentArgs by navArgs()

    private val touchCallback: SwipeTouchCallback by lazy {
        object : SwipeTouchCallback() {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = GroupAdapter<GroupieViewHolder<NewJobItemBinding>>().getItem(viewHolder.layoutPosition)
                // Change notification to the adapter happens automatically when the section is
                // changed.

                swipeSection.remove(item)
            }
        }
    }

    init {
        lifecycleScope.launch {
            whenCreated {
                uiScope.onCreate()
            }
            whenStarted {
                lifecycle.addObserver(uiScope)
                initViewModels()
                uiUpdate()
                requireActivity().hideKeyboard()
            }
            whenResumed {
                if (!this@AddProjectFragment::job.isInitialized && !addProjectArgs.jobId.isNullOrEmpty()) {
                    onRestoreInstanceState(addProjectArgs.toBundle())
                    uiUpdate()
                    stateRestored = true
                }
            }
        }
    }

    companion object {
        private const val JOB_KEY = "jobId"
        private const val PROJECT_KEY = "projectId"
    }

    private fun initViewModels() {
        createViewModel = activity?.run {
            ViewModelProvider(this, createFactory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        unsubmittedViewModel = activity?.run {
            ViewModelProvider(this, unsubFactory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        deferredLocationViewModel = activity?.run {
            ViewModelProvider(this, deferredLocationFactory).get(DeferredLocationViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            !addProjectArgs.jobId.isNullOrBlank() -> {
                onRestoreInstanceState(addProjectArgs.toBundle())
            }
            savedInstanceState != null && !stateRestored -> {
                onRestoreInstanceState(savedInstanceState)
            }
            else -> {
                stateRestored = false
                (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
                newJobItemEstimatesList = ArrayList()
                // Generic new job layout
                ui.lastLin.visibility = View.GONE
                ui.totalCostTextView.visibility = View.GONE
                ui.dueDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
                ui.startDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
                startDate = DateUtil.currentDateTime!!
                dueDate = DateUtil.currentDateTime!!
            }
        }

        ItemTouchHelper(touchCallback).attachToRecyclerView(ui.projectRecyclerView)
        setmyClickListener()
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * [.setRetainInstance] to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after [.onCreateView]
     * and before [.onViewStateRestored].
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     *
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        uiUpdate()
    }

    fun uiUpdate() {
        jobBound = false
        uiScope.launch(uiScope.coroutineContext) {
            initCurrentJobListener()
            initValidationListener()
            initCurrentUserObserver()
        }
    }

    private fun initCurrentUserObserver() {
        createViewModel.loggedUser.observe(viewLifecycleOwner, { userId ->
            userId?.let {

                this@AddProjectFragment.userId = it.toString()
            }
        })
    }

    private suspend fun initValidationListener() = withContext(Dispatchers.Main) {
        deferredLocationViewModel.geoCodingResult.distinctUntilChanged().observeOnce(
            viewLifecycleOwner, { result ->
                result?.let { outcome ->
                    uiScope.launch(uiScope.coroutineContext) {
                        processLocationResult(outcome)
                    }
                }
            })

        createViewModel.jobForValidation.distinctUntilChanged().observeOnce(
            viewLifecycleOwner, { job ->
                job.getContentIfNotHandled()?.let { realJob ->
                    if (!realJob.sectionId.isNullOrBlank() && JobUtils.isGeoCoded(realJob)) {
                        uiScope.launch(uiScope.coroutineContext) {
                            validateEstimates(realJob)
                        }
                    } else {
                        uiScope.launch(uiScope.coroutineContext) {
                            geoLocationFailed()
                        }
                    }
                }
            })

        createViewModel.jobForSubmission.distinctUntilChanged().observeOnce(
            viewLifecycleOwner, { unsubmittedEvent ->
                unsubmittedEvent.getContentIfNotHandled()?.let { submissionJob ->
                    uiScope.launch(uiScope.coroutineContext) {
                        createViewModel.backupSubmissionJob.value = XIEvent(submissionJob)
                        submitJob(submissionJob)
                    }
                }
            }
        )
    }

    private fun initCurrentJobListener() {
        var itemsBound = false
        val currentJobQuery = createViewModel.currentJob.distinctUntilChanged()
        currentJobQuery.observe(viewLifecycleOwner, { jobEvent ->
            jobEvent.getContentIfNotHandled()?.let { jobToEdit ->
                job = jobToEdit
                jobBound = true
                Coroutines.main {
                    if (createViewModel.jobDesc != job.descr) {
                        toast("Editing ${job.descr}")
                        createViewModel.jobDesc = job.descr
                    }
                    projectID = job.projectId
                    val contractNo =
                        createViewModel.getContractNoForId(job.contractVoId)
                    val projectCode =
                        createViewModel.getProjectCodeForId(job.projectId)
                    ui.selectedContractTextView.text = contractNo
                    ui.selectedProjectTextView.text = projectCode

                    ui.infoTextView.visibility = View.GONE
                    ui.lastLin.visibility = View.VISIBLE
                    ui.totalCostTextView.visibility = View.VISIBLE

                    // Set job description init actionBar
                    (activity as MainActivity).supportActionBar?.title = createViewModel.jobDesc

                    // Set Job Start & Completion Dates

                    job.dueDate?.let {
                        ui.dueDateTextView.text = DateUtil.toStringReadable(DateUtil.stringToDate(it))
                        dueDate = DateUtil.stringToDate(it)!!
                    }

                    job.startDate?.let {
                        ui.startDateTextView.text = DateUtil.toStringReadable(DateUtil.stringToDate(it))
                        startDate = DateUtil.stringToDate(it)!!
                    }
                }
            }
        })

        createViewModel.tempProjectItem.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let {
                ui.infoTextView.visibility = View.GONE
                ui.lastLin.visibility = View.VISIBLE
                ui.totalCostTextView.visibility = View.VISIBLE
                if (this@AddProjectFragment::job.isInitialized) {
                    bindProjectItems()
                    itemsBound = true
                }
            }
        })

        if (this@AddProjectFragment::job.isInitialized && !itemsBound) {
            bindProjectItems()
        }
    }

    private fun bindProjectItems() = uiScope.launch(uiScope.coroutineContext) {
        val projectItems =
            createViewModel.getAllProjectItems(projectID!!, job.jobId)
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

    private fun populateProjectItemView(itemList: List<ItemDTOTemp>) {
        items = itemList.filter { itemDTOTemp ->
            itemDTOTemp.jobId == job.jobId
        }
        when {
            items.isNotEmpty() -> {
                initRecyclerView(items.toProjecListItems())
                calculateTotalCost()
            }
            else -> {
                clearProjectItems()
            }
        }
    }

    private fun clearProjectItems() {
        groupAdapter.clear()
        ui.totalCostTextView.text = ""
        ui.lastLin.visibility = View.GONE
        ui.totalCostTextView.visibility = View.GONE
    }

    private fun bindCosting() {
        uiScope.launch(uiScope.coroutineContext) {
            createViewModel.estimateLineRate.observe(
                viewLifecycleOwner,
                {
                    calculateTotalCost()
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        groupAdapter = GroupAdapter<GroupieViewHolder<NewJobItemBinding>>()
        jobDataController = JobDataController

        initViewModels()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) item2.isVisible = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentAddProjectItemsBinding.inflate(inflater, container, false)
        return ui.root
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.run {
            val jobId = getSerializable(JOB_KEY) as String?
            val projectId = getSerializable(PROJECT_KEY) as String?
            Coroutines.main {
                projectId?.let { restoredProjectId ->
                    projectID = restoredProjectId
                }
                jobId?.let { restoredId ->
                    Coroutines.main {
                        createViewModel.setJobToEdit(restoredId)
                        withContext(Dispatchers.Main.immediate) {
                            uiUpdate()
                        }
                    }
                }
            }
            stateRestored = true
        }
    }

    private fun initRecyclerView(projecListItems: List<ProjectItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<NewJobItemBinding>>().apply {
            addAll(projecListItems)
        }

        ui.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<ItemDTOTemp>.toProjecListItems(): List<ProjectItem> {
        return this.map {
            ProjectItem(
                fragment = this@AddProjectFragment,
                itemDesc = it,
                createViewModel = createViewModel,
                contractID = contractID,
                job = job
            )
        }
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
                        if (this@AddProjectFragment.requireContext().isConnected) {
                            initValidationListener()
                            validateJob()
                        } else {
                            displayConnectionWarning()
                        }
                    }
                }
            }
        }

        ui.addItemButton.setOnClickListener(myClickListener)
        ui.resetButton.setOnClickListener(myClickListener)
        ui.startDateCardView.setOnClickListener(myClickListener)
        ui.dueDateCardView.setOnClickListener(myClickListener)
        ui.submitButton.setOnClickListener(myClickListener)
        ui.infoTextView.setOnClickListener(myClickListener)
    }

    private fun displayConnectionWarning() {
        this@AddProjectFragment.extensionToast(
            title = "No Connectivity",
            message = "Job validation and submission requires an active internet connection.",
            style = ToastStyle.NO_INTERNET,
            duration = ToastDuration.LONG,
            position = ToastGravity.BOTTOM
        )
    }

    private fun openSelectItemFragment(view: View) {
        if (jobBound) {
            backupJobInProgress(job)
            createViewModel.setItemJob(job.jobId)
        }

        val navDirection =
            AddProjectFragmentDirections.actionAddProjectFragmentToSelectItemFragment(
                projectID
            )
        Navigation.findNavController(view)
            .navigate(navDirection)
    }

    private suspend fun validateJob() = uiScope.launch(uiScope.coroutineContext) {

        if (job.jobId.isNotEmpty() && validateCalendar()) {

            withContext(Dispatchers.Main) {
                if (!JobUtils.areQuantitiesValid(job)) {
                    this@AddProjectFragment.extensionToast(
                        message = "Error: incomplete estimates.\n Quantity can't be zero!",
                        style = WARNING
                    )
                    ui.itemsCardView.startAnimation(shake_long)
                } else {
                    validateLocations(job)
                }
            }
        }
    }

    private suspend fun processLocationResult(result: XIResult<String>) = withContext(Dispatchers.Main) {
        when (result) {
            is XIResult.Success -> {
                handleGeoSuccess(result)
            }
            is XIResult.Error -> {
                handleGeoError(result)
            }
            else -> geoLocationFailed()
        }
    }

    private suspend fun AddProjectFragment.handleGeoError(
        result: XIResult.Error
    ) = withContext(Dispatchers.Main) {
        this@AddProjectFragment.extensionToast(
            title = "Location Validation",
            message = result.exception.message.toString(),
            style = ERROR
        )
        crashGuard(this@AddProjectFragment.requireView(), result,
            refreshAction = { Coroutines.main { this@AddProjectFragment.validateJob() } })
        geoLocationFailed()
    }

    private fun handleGeoSuccess(result: XIResult.Success<String>) {
        val geoCodedJobId = result.data
        createViewModel.setJobToValidate(geoCodedJobId)
    }

    private suspend fun geoLocationFailed() = withContext(Dispatchers.Main.immediate) {
        ui.submitButton.failProgress("Locations not verified ...")
        toggleLongRunning(false)
        ui.itemsCardView.startAnimation(shake_long)
        onGeoLocationFailed()
    }

    private suspend fun validateLocations(job: JobDTO) = withContext(Dispatchers.Main) {
        createViewModel.backupJob(job)
        toggleLongRunning(true)
        ui.submitButton.initProgress(viewLifecycleOwner)
        ui.submitButton.startProgress("Checking locations ...")
        deferredLocationViewModel.checkLocations(job.jobId)
    }

    private suspend fun validateEstimates(updatedJob: JobDTO) = withContext(Dispatchers.Main) {
        val validated = createViewModel.areEstimatesValid(updatedJob, ArrayList(items))

        if (!validated) {
            onInvalidJob()
        } else {
            withContext(Dispatchers.Main.immediate) {
                ui.submitButton.initProgress(viewLifecycleOwner)
                ui.submitButton.startProgress("Submitting data ...")
                updatedJob.issueDate = DateUtil.dateToString(Date())
                createViewModel.backupJob(updatedJob)
                createViewModel.setJobForSubmission(updatedJob.jobId)
            }
        }
    }

    private fun onGeoLocationFailed() {
        Coroutines.ui {
            val errorToast = ColorToast(
                title = "Location Verification Failed",
                message = "One or more work locations could not be verified.\n" +
                    "Check estimates for more information.",
                style = ERROR,
                gravity = ToastGravity.CENTER,
                duration = ToastDuration.LONG
            )
            this@AddProjectFragment.extensionToast(errorToast)
            HandlerCompat.postDelayed(Handler(Looper.getMainLooper()), {
                createViewModel.setJobToEdit(job.jobId)
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
                extensionToast(message = "Please select a valid due date", style = WARNING)
                ui.dueDateCardView.startAnimation(shake_long)
            } else {
                job.dueDate
                dueResult = true
            }
        } else {
            extensionToast(message = "Please select a Due Date", style = WARNING)
            ui.dueDateCardView.startAnimation(shake_long)
        }
        if (job.startDate != null) {
            if (startDate < yesterday || dueDate == yesterday
            ) {
                extensionToast(message = "Please select a valid Start Date", style = WARNING)
                ui.dueDateCardView.startAnimation(shake_long)
            } else {
                job.startDate
                startResult = true
            }
        } else {
            extensionToast(message = "Please select Start Date", style = WARNING)
            ui.startDateCardView.startAnimation(shake_long)
        }

        return startResult && dueResult
    }

    private fun selectDueDate() {
        ui.dueDateCardView.startAnimation(click) // Get Current Date
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
        ui.startDateCardView.startAnimation(click)
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

    @Synchronized
    private suspend fun submitJob(
        job: JobDTO
    ) = withContext(uiScope.coroutineContext) {
        toggleLongRunning(true)
        val jobTemp = jobDataController.setJobLittleEndianGuids(job)
        saveRrmJob(job.userId, jobTemp)
    }

    @Synchronized
    private suspend fun saveRrmJob(
        userId: Int,
        job: JobDTO
    ) = withContext(uiScope.coroutineContext) {
        val submit =
            createViewModel.submitJob(userId, job, requireActivity())
        withContext(Dispatchers.Main.immediate) {
            toggleLongRunning(false)
            if (submit.isNotBlank()) {
                this@AddProjectFragment.extensionToast(
                    message = submit,
                    style = ERROR
                )
            } else {
                this@AddProjectFragment.extensionToast(
                    message = getString(R.string.job_submitted),
                    style = ToastStyle.SUCCESS
                )
                popViewOnJobSubmit()
            }
        }
    }

    private fun popViewOnJobSubmit() {
        // Delete Items data from the database after success upload
        // onResetClicked(this.requireView())
        // createViewModel.setJobToEdit("null")
        // Conduct user back to home fragment
        HandlerCompat.postDelayed(Handler(Looper.getMainLooper()), {
            Intent(context?.applicationContext, MainActivity::class.java).also { home ->
                startActivity(home)
            }
        }, null, TWO_SECONDS)
    }

    private fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        ui.dueDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        ui.dueDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job.dueDate = DateUtil.dateToString(Date.from(calendar.toInstant()))
        backupJobInProgress(job)
    }

    private fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        ui.startDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        ui.startDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job.startDate = DateUtil.dateToString(Date.from(calendar.toInstant()))
        backupJobInProgress(job)
    }

    private fun backupJobInProgress(jobDTO: JobDTO?) {
        Coroutines.main {
            createViewModel.backupJob(jobDTO!!)
        }
    }

    private fun onResetClicked(view: View) {
        Coroutines.main {
            createViewModel.deleteItemList(job.jobId)
            createViewModel.deleteJobFromList(job.jobId)
            // createViewModel.setCurrentJob(null)
        }
        resetContractAndProjectSelection(view)
    }

    private fun calculateTotalCost() {
        Coroutines.main {
            ui.totalCostTextView.text = JobUtils.formatTotalCost(job)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this::job.isInitialized) {
            outState.putString(JOB_KEY, job.jobId)
            outState.putString(PROJECT_KEY, job.projectId)
            super.onSaveInstanceState(outState)
        }
    }

    private fun resetContractAndProjectSelection(view: View) {
        Navigation.findNavController(view).popBackStack(R.id.nav_create, false)
        ui.infoTextView.text = null
    }

    private fun onInvalidJob() {
        toggleLongRunning(false)
        extensionToast(message = "Incomplete estimates!", style = WARNING)
        ui.itemsCardView.startAnimation(shake_long)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        createViewModel.currentJob.removeObservers(viewLifecycleOwner)
        createViewModel.tempProjectItem.removeObservers(viewLifecycleOwner)
        deferredLocationViewModel.geoCodingResult.removeObservers(viewLifecycleOwner)
        createViewModel.jobForValidation.removeObservers(viewLifecycleOwner)
        createViewModel.jobForSubmission.removeObservers(viewLifecycleOwner)
        uiScope.destroy()
        ui.projectRecyclerView.adapter = null
        _ui = null
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() has been called.")
    }
}
