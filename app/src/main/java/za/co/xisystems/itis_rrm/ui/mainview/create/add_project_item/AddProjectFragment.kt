@file:Suppress("RemoveExplicitTypeArguments")

package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentAddProjectItemsBinding
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SwipeTouchCallback
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.WARNING
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

/**
 * Created by Francis Mahlava on 2019/12/29.
 */
//
class AddProjectFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private val unsubFactory: UnSubmittedViewModelFactory by instance<UnSubmittedViewModelFactory>()
    private val createFactory: CreateViewModelFactory by instance<CreateViewModelFactory>()
    private var dueDateDialog: DatePickerDialog? = null
    private var startDateDialog: DatePickerDialog? = null
    private lateinit var jobDataController: JobDataController
    private val swipeSection = Section()
    private var contractID: String? = null
    private var projectID: String? = null
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var startDate: Date
    private lateinit var dueDate: Date
    private var _ui: FragmentAddProjectItemsBinding? = null
    private val ui get() = _ui!!

    @MyState
    private var job: JobDTO? = null

    @MyState
    private var items: List<ItemDTOTemp> = ArrayList()

    // TODO: Restore to last state saved when user arrives here by clicking the back button.

    init {
        lifecycleScope.launch {

            whenStarted {
                createViewModel.newJob.observe(viewLifecycleOwner, { newJ ->
                    newJ?.let {
                        job = it
                        projectID = it.ProjectId
                    }
                })

                createViewModel.currentJob.observe(
                    viewLifecycleOwner,
                    { currentJob ->
                        currentJob?.let { jobToEdit ->
                            toast("Editing ${jobToEdit.Descr}")
                            Coroutines.main {
                                projectID = jobToEdit.ProjectId
                                job = jobToEdit
                                val contractNo =
                                    createViewModel.getContractNoForId(jobToEdit.ContractVoId)
                                val projectCode =
                                    createViewModel.getProjectCodeForId(jobToEdit.ProjectId)
                                ui.selectedContractTextView.text = contractNo
                                ui.selectedProjectTextView.text = projectCode

                                ui.infoTextView.visibility = View.GONE
                                ui.lastLin.visibility = View.VISIBLE
                                ui.totalCostTextView.visibility = View.VISIBLE

                                // createViewModel.setJobToEdit(currentJob.JobId)

                                // Set job description init actionBar
                                (activity as MainActivity).supportActionBar?.title = jobToEdit.Descr

                                Coroutines.main {
                                    val projectItemData =
                                        createViewModel.getAllProjectItems(projectID!!, job!!.JobId)
                                    projectItemData.observe(
                                        viewLifecycleOwner,
                                        { projectItemList ->
                                            if (projectItemList.isEmpty()) {
                                                groupAdapter.clear()
                                                ui.totalCostTextView.text = ""
                                                ui.lastLin.visibility = View.GONE
                                                ui.totalCostTextView.visibility = View.GONE
                                            }
                                            items = projectItemList
                                            for (item in projectItemList.listIterator()) {
                                                if (job?.JobId != item.jobId) {
                                                    groupAdapter.clear()
                                                    ui.totalCostTextView.clearComposingText()
                                                } else {
                                                    initRecyclerView(projectItemList.toProjecListItems())
                                                    calculateTotalCost()
                                                }
                                            }
                                        })
                                }
                            }
                        }
                    })

                createViewModel.sectionProjectItem.observe(viewLifecycleOwner, {
                    ui.infoTextView.visibility = View.GONE
                    ui.lastLin.visibility = View.VISIBLE
                    ui.totalCostTextView.visibility = View.VISIBLE

                    Coroutines.main {
                        val projectItems =
                            createViewModel.getAllProjectItems(projectID!!, job!!.JobId)
                        projectItems.observe(viewLifecycleOwner, { itemList ->
                            if (itemList.isEmpty()) {
                                groupAdapter.clear()
                                ui.totalCostTextView.text = ""
                                ui.lastLin.visibility = View.GONE
                                ui.totalCostTextView.visibility = View.GONE
                            } else {
                                items = itemList
                            }

                            for (item in items.listIterator()) {
                                if (item.jobId != job?.JobId) {
                                    items.drop(item.id)
                                    groupAdapter.clear()
                                    groupAdapter.notifyDataSetChanged()
                                    ui.totalCostTextView.clearComposingText()
                                } else {
                                    initRecyclerView(items.toProjecListItems())
                                }
                            }
                        })
                        createViewModel.estimateLineRate.observe(
                            viewLifecycleOwner,
                            {
                                calculateTotalCost()
                            })
                    }
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        groupAdapter = GroupAdapter<GroupieViewHolder>()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
        jobDataController = JobDataController
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
        return false // To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        calculateTotalCost()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentAddProjectItemsBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        createViewModel = activity?.run {
            ViewModelProvider(this, createFactory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        unsubmittedViewModel = activity?.run {
            ViewModelProvider(this, unsubFactory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        ui.lastLin.visibility = View.GONE
        ui.totalCostTextView.visibility = View.GONE
        ui.dueDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
        ui.startDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
        startDate = DateUtil.currentDateTime!!
        dueDate = DateUtil.currentDateTime!!

        Coroutines.main {
            val contractNo = createViewModel.getContractNoForId(job?.ContractVoId)
            val projectCode = createViewModel.getProjectCodeForId(job?.ProjectId)
            ui.selectedContractTextView.text = contractNo
            ui.selectedProjectTextView.text = projectCode
        }

        ItemTouchHelper(touchCallback).attachToRecyclerView(ui.projectRecyclerView)
        setmyClickListener()
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.run {
            val job = getSerializable("job") as JobDTO
            val items = getSerializable("items") as List<ItemDTOTemp>
            newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
            createViewModel.setCurrentJob(job)
            initRecyclerView(items.toProjecListItems())
        }
    }

    private fun initRecyclerView(projecListItems: List<ProjectItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
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
                val item = GroupAdapter<GroupieViewHolder>().getItem(viewHolder.layoutPosition)
                // Change notification to the adapter happens automatically when the section is
                // changed.

                swipeSection.remove(item)
            }
        }
    }

    private fun setmyClickListener() {
        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.addItemButton -> {
                    createViewModel.newJob.observe(viewLifecycleOwner, { newJ ->
                        newJ?.let {
                            projectID = it.ProjectId
                            job = it
                        }
                    })

                    Navigation.findNavController(view)
                        .navigate(R.id.action_addProjectFragment_to_selectItemFragment)
                }

                R.id.resetButton -> {
                    onResetClicked(view)
                }

                R.id.infoTextView -> {
                    if (view.visibility == View.VISIBLE)
                        Navigation.findNavController(view)
                            .navigate(R.id.action_addProjectFragment_to_selectItemFragment)
                }

                R.id.startDateCardView -> {
                    selectStartDate()
                }

                R.id.dueDateCardView -> {
                    selectDueDate()
                }

                R.id.submitButton -> {

                    validateJob()
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

    private fun validateJob() {

        if (job != null && validateCalendar()) {

            Coroutines.main {
                if (!JobUtils.areQuantitiesValid(job)) {
                    this@AddProjectFragment.sharpToast(
                        message = "Error: incomplete estimates.\n Quantity can't be zero!",
                        style = WARNING
                    )
                    ui.itemsCardView.startAnimation(shake_long)
                } else {
                    val valid =
                        createViewModel.areEstimatesValid(job, ArrayList<Any?>(items))
                    if (!valid) {
                        onInvalidJob()
                    } else {
                        ui.submitButton.initProgress(viewLifecycleOwner)
                        ui.submitButton.startProgress("Submitting data ...")

                        job!!.IssueDate = Date().toString()
                        submitJob(job!!)
                    }
                }
            }
        }
    }

    private fun validateCalendar(): Boolean {

        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1) // number represents number of days
        val yesterday = cal.time

        var startResult = false
        var dueResult = false

        if (job?.DueDate != null) {

            if (dueDate < startDate || dueDate < yesterday || dueDate == yesterday
            ) {
                toast("Please select a valid due date")
                ui.dueDateCardView.startAnimation(shake_long)
            } else {
                job?.DueDate
                dueResult = true
            }
        } else {
            toast("Please select Due Date")
            ui.dueDateCardView.startAnimation(shake_long)
        }
        if (job!!.StartDate != null) {
            if (startDate < yesterday || dueDate == yesterday
            ) {
                toast("Please select a valid Start Date")
                ui.dueDateCardView.startAnimation(shake_long)
            } else {
                job!!.StartDate
                startResult = true
            }
        } else {
            toast("Please select Start Date")
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
                    dueDate = Date(year, month, dayOfMonth)
                }, year, month, day
            )
        }
        dueDateDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
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
        startDateDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
        startDateDialog!!.show()
    }

    private fun submitJob(
        job: JobDTO
    ) {
        toggleLongRunning(true)
        val jobTemp = jobDataController.setJobLittleEndianGuids(job)!!
        saveRrmJob(job.UserId, jobTemp)
    }

    private fun saveRrmJob(
        userId: Int,
        job: JobDTO
    ) {
        Coroutines.main {
            val submit =
                createViewModel.submitJob(userId, job, requireActivity())
            toggleLongRunning(false)
            if (submit.isNotBlank()) {
                this@AddProjectFragment.sharpToast(
                    message = submit,
                    style = ToastStyle.ERROR
                )
            } else {
                this@AddProjectFragment.sharpToast(
                    message = getString(R.string.job_submitted),
                    style = ToastStyle.SUCCESS
                )
                popViewOnJobSubmit()
            }
        }
    }

    private fun popViewOnJobSubmit() {
        // TODO: delete Items data from the database after success upload
        Intent(context?.applicationContext, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    private fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        ui.dueDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        Timber.d("")
        // dueDate = DateUtil.CalendarItemsToDate(year,month,dayOfMonth)!!
        ui.dueDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job?.DueDate = calendar.time.toString()
        backupJobInProgress(job)
    }

    private fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        ui.startDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
//        startDate = DateUtil.CalendarItemsToDate(year, month, dayOfMonth)!!
        ui.startDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job?.StartDate = calendar.time.toString()
        backupJobInProgress(job)
    }

    private fun backupJobInProgress(jobDTO: JobDTO?) {
        Coroutines.main {
            createViewModel.backupJob(jobDTO!!)
        }
    }

    private fun onResetClicked(view: View) {
        Coroutines.main {
            createViewModel.deleteItemList(job!!.JobId)
            createViewModel.deleteJobFromList(job!!.JobId)
        }
        resetContractAndProjectSelection(view)
    }

    private fun calculateTotalCost() {
        ui.totalCostTextView.text = JobUtils.formatTotalCost(job)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("job", job as JobDTO)
        outState.putSerializable("items", items as ArrayList<ItemDTOTemp>)
        super.onSaveInstanceState(outState)
    }

    private fun resetContractAndProjectSelection(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_addProjectFragment_to_nav_create)
        ui.infoTextView.text = null
    }

    private fun onInvalidJob() {
        sharpToast(message = "Incomplete estimates!", style = WARNING)
        ui.itemsCardView.startAnimation(shake_long)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.projectRecyclerView.adapter = null
        _ui = null
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onDestroyView() has been called.")
    }
}
