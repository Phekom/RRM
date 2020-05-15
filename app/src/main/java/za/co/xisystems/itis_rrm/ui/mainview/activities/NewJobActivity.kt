//package za.co.xisystems.itis_rrm.ui.mainview.activities
//
//import android.app.AlertDialog
//import android.app.DatePickerDialog
//import android.content.Context
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import androidx.lifecycle.ViewModelProviders
//import com.google.android.gms.common.api.GoogleApiClient
//import kotlinx.android.synthetic.main.activity_new_job.*
//import org.kodein.di.KodeinAware
//import org.kodein.di.android.kodein
//import org.kodein.di.generic.instance
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data.localDB.entities.*
//import za.co.xisystems.itis_rrm.data.network.OfflineListener
//import za.co.xisystems.itis_rrm.ui.models.CreateViewModel
//import za.co.xisystems.itis_rrm.ui.models.CreateViewModelFactory
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.IJobSubmit
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IEstimatesAdapter
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IJobOfflineHelper
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.INewJobActivity
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.INewJobSelectItemIntent
//import za.co.xisystems.itis_rrm.utils.*
//import java.util.*
//
//class NewJobActivity : NewJobBase(),
//    INewJobActivity<NewJobActivity>,OfflineListener, KodeinAware, IJobSubmit {
//
//    override val kodein by kodein()
//    private lateinit var createViewModel: CreateViewModel
//    private val factory: CreateViewModelFactory by instance()
//
//    var offlineHelper: IJobOfflineHelper? = null
//    private val appContext : Context? = null
//    var newJobSelectItemIntent: INewJobSelectItemIntent? = null
//
//    private val adapter: IEstimatesAdapter<ProjectItemDTO>? = null
//    private var saveMenuItem: MenuItem? = null
//    private  var deleteMenuItem: MenuItem? = null
//    private  var resetMenuItem: MenuItem? = null
//    private val estimatesToRemoveFromDb: ArrayList<JobItemEstimateDTO> =
//        ArrayList<JobItemEstimateDTO>()
//
//    var dueDateDialog: DatePickerDialog? = null
//    var startDateDialog: DatePickerDialog? = null
//
//
//    @MyState
//    var items: ArrayList<ProjectItemDTO> = ArrayList<ProjectItemDTO>()
//    @MyState
//    internal var selectedContract: ContractDTO? = null
//    //        internal var selectedContract: String? = null
//    @MyState
//    internal var selectedProject: ProjectDTO? = null
//    //        internal var selectedProject: String? = null
//    @MyState
//    internal var selectedProjectitem: ProjectItemDTO? = null
////    internal var selectedProjectitem: String? = null
//
//    @MyState
//    internal var job: JobDTO? = null
//    @MyState
//    var isJobSaved = false
//    private val client: GoogleApiClient? = null
//
//    var rListener: OfflineListener? = null
//
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.new_job, menu)
//        resetMenuItem = menu.findItem(R.id.mi_reset)
//        deleteMenuItem = menu.findItem(R.id.mi_delete)
//        saveMenuItem = menu.findItem(R.id.mi_save)
//        resetMenuItem?.setVisible(false)
//        deleteMenuItem?.setVisible(false)
//        saveMenuItem?.setVisible(job != null && items != null && !items.isEmpty())
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.mi_save -> {
//                val logoutBuilder =
//                    AlertDialog.Builder(
//                        this,
//                        android.R.style.Theme_DeviceDefault_Dialog
//                    )
//                logoutBuilder.setTitle(R.string.confirm)
//                logoutBuilder.setIcon(R.drawable.ic_error)
//                logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_save)
//                // Yes button
//                logoutBuilder.setPositiveButton(
//                    R.string.yes
//                ) { dialog, which -> saveJob() }
//                // No button
//                logoutBuilder.setNegativeButton(
//                    R.string.no
//                ) { dialog, which ->
//                    // Do nothing but close dialog
//                    dialog.dismiss()
//                }
//                val declineAlert = logoutBuilder.create()
//                declineAlert.show()
//            }
//            R.id.mi_reset -> onResetClicked()
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_new_job)
//        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
//
////        setContentView(R.layout.fragment_createjob)
//        setSupportActionBar(newjob_toolbar)
//
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setDisplayShowHomeEnabled(true)
//        }
//
//        mid_lin.visibility = View.GONE
//        last_lin.visibility = View.GONE
//        selectProjectItemdrop.visibility = View.GONE
//        photoLin.visibility = View.GONE
//
//
//        val myClickListener = View.OnClickListener { view ->
//            when (view?.id) {
//                R.id.selectContractProjectContinueButton -> {
//                    val description = descriptionEditText.text!!.toString().trim { it <= ' ' }
//                    if (description.isEmpty()) {
//                        toast("Error: description missing")
//                        descriptionEditText.startAnimation(shake)
//                        //                            return
//                    }else{
//                        hideKeyboard()
//                        job?.IssueDate   = (Calendar.getInstance().time).toString()
//                        job?.Descr = description
//                        //                        val job = JobDTO
//                        setContractAndProjectSelection(true)
//                    }
//
//                }
//
//                R.id.infoTextView -> {
////                    selectProjectLayout.visibility = View.GONE
////                    mid_lin.visibility = View.GONE
////                    photoLin.visibility = View.GONE
////                    selectProjectdrop.visibility = View.VISIBLE
////                    last_lin.visibility = View.GONE
//                }
//
//                R.id.addItemButton -> {
//                    if (selectedProject == null) {
//                        toast("Error: selectedProject is null!")
//                    } else {
//                        val projectId: String = selectedProject!!.projectId
//                        newJobSelectItemIntent?.startActivityForResult(
//                            this@NewJobActivity,
//                            projectId)
//                        selectProjectItemdrop.visibility = View.VISIBLE
////                                                    selectProjectLayout.visibility = View.GONE
//                                                    mid_lin.visibility = View.GONE
////                                                    photoLin.visibility = View.GONE
////                                                    last_lin.visibility = View.GONE
//
//                    }
//
//                }
//
//                R.id.updateButton -> {
////                    infoTextView.visibility = View.VISIBLE
////                    selectProjectdrop.visibility = View.GONE
////                    photoLin.visibility = View.GONE
////                    mid_lin.visibility = View.VISIBLE
////                    last_lin.visibility = View.VISIBLE
//                }
//
//                R.id.resetButton -> {
//                    onResetClicked()
//                }
//
//
//            }
//        }
//
//
//        selectContractProjectContinueButton.setOnClickListener(myClickListener)
////        selectContractProjectContinueButton.setOnClickListener(myClickListener)
//        infoTextView.setOnClickListener(myClickListener)
//        addItemButton.setOnClickListener(myClickListener)
//        updateButton.setOnClickListener(myClickListener)
//        resetButton?.setOnClickListener(myClickListener)
//
//        setContract()
//
//
//
//    }
//
//    private fun setContractAndProjectSelection(animate: Boolean) {
////        if (selectedContract != null && selectedProject != null && job != null) {
//        if (selectedContract != null && selectedProject != null) {
//            selectProjectLayout.visibility = View.GONE
//            mid_lin.visibility = View.VISIBLE
//            if (animate) {
//                addItemLayout.startAnimation(bounce_750)
//                itemsCardView.startAnimation(bounce_500)
//            }
//
//            selectedContractTextView.text = selectedContract?.contractNo.toString()
//            selectedProjectTextView.text = selectedProject?.projectCode.toString()
//            job?.ContractVoId = selectedContract!!.contractId
//            job?.ProjectId = selectedProject!!.projectId
//            setMenuItems()
//        }
//    }
//
//
//    fun setMenuItems() {
//        if (saveMenuItem != null) {
//            saveMenuItem?.isVisible = job != null && items != null && items.isNotEmpty()
//        }
//        setResetButton()
//    }
//
//    private fun setResetButton() {
//        if (isJobSaved) resetButton?.visibility = View.GONE else resetButton?.visibility =
//            View.VISIBLE
//    }
//    private fun calculateTotalCost() {
////        totalCostTextView.setText(JobUtils.formatTotalCost(job))
//    }
//    private fun setLayoutsVisibility() {
//        val hasItems = adapter != null && adapter.itemCount > 0
//        infoTextView.visibility = if (hasItems) View.GONE else View.VISIBLE
//        submitButton.visibility = if (hasItems) View.VISIBLE else View.GONE
//        dueDateCardView.visibility = if (hasItems) View.VISIBLE else View.GONE
//        startDateCardView.visibility = if (hasItems) View.VISIBLE else View.GONE
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putSerializable("selectedContract", selectedContract)
//        outState.putSerializable("selectedProject", selectedProject)
//        outState.putSerializable("job", job)
//        outState.putSerializable("items", selectedProjectitem)
//        outState.putBoolean("isJobSaved", isJobSaved)
//        outState.putSerializable("estimatesToRemoveFromDb", estimatesToRemoveFromDb)
//        super.onSaveInstanceState(outState)
//    }
//
//    private fun setContract() {
//        Coroutines.main {
//            data_loading.show()
//            try {
//                val contracts = createViewModel.getContracts()
////                val contracts = authViewModel.offlinedata.await()
//                contracts.observe(this, androidx.lifecycle.Observer {contrac_t ->
//                    data_loading.hide()
//                    val contractId = contrac_t
//                    val contractNmbr = arrayOfNulls<String>(contrac_t.size)
//                    for (contract in contrac_t.indices) {
//                        contractNmbr[contract] = contrac_t[contract].contractNo
//                    }
//
//                    SpinnerHelper.setSpinner(this.applicationContext,
//                        contractSpinner,
//                        contractId,
//                        contractNmbr, //null)
//                        object : SpinnerHelper.SelectionListener<ContractDTO> {
//                            override fun onItemSelected(position: Int, item: ContractDTO) {
//                                if (item == null)
//                                   toast("Error: Contract is NULL")
//                                else {
//                                    selectedContract = item
//                                    setProjects(item.contractId)
////                                    selectedContractTextView.setText(selectedContract?.contractNo)
//                                }
//                            }
//
//                        })
//
//                })
//
//            } catch (e: ApiException) {
//                toast(e.message!!)
//            } catch (e: NoInternetException) {
//                toast(e.message!!)
//            }
//        }
//    }
//
//    private fun setProjects(contractId: String?) {
//        Coroutines.main {
//            try {
//                val projects = createViewModel.getSomeProjects(contractId!!)
////                val projects = authViewModel.projects.await()
//                projects.observe(this, androidx.lifecycle.Observer { projec_t ->
//                    val projects = projec_t
//                    val projectNmbr = arrayOfNulls<String>(projec_t.size)
//                    for (project in projec_t.indices) {
//                        projectNmbr[project] = projec_t[project].projectCode
//                    }
//                    SpinnerHelper.setSpinner(this.applicationContext,
//                        projectSpinner,
//                        projects,
//                        projectNmbr, //null)
//                        object : SpinnerHelper.SelectionListener<ProjectDTO> {
//                            override fun onItemSelected(position: Int, item: ProjectDTO) {
//
//                                if (item == null)
//                                    toast("Error: Project is NULL")
//                                else {
//                                    selectedProject = item
//                                    setItems(item.projectId)
////                                    selectedProjectTextView.setText(selectedProject?.projectCode)
//                                }
//                            }
//                        })
//
//                })
//
//            } catch (e: ApiException) {
//                toast(e.message!!)
//            } catch (e: NoInternetException) {
//                toast(e.message!!)
//            }
//        }
//
//    }
//
//    private fun setItems(projectId: String) {
//        Coroutines.main {
//            try {
//                val projectsItems = createViewModel.getAllItemsForProjectId(projectId)
////                val projectsItems = authViewModel.projectsItems.await()
//                projectsItems?.observe(this, androidx.lifecycle.Observer { i_tems ->
//                    val items = i_tems
//                    val itemNmbr = arrayOfNulls<String>(i_tems.size)
//                    for (project in i_tems.indices) {
//                        itemNmbr[project] =
//                            i_tems[project].itemCode + " " + i_tems[project].descr + " " + i_tems[project].uom
//                    }
//                    SpinnerHelper.setSpinner(this.applicationContext,
//                        sectionItemSpinner,
//                        items,
//                        itemNmbr,
//                        object : SpinnerHelper.SelectionListener<ProjectItemDTO> {
//                            override fun onItemSelected(position: Int, item: ProjectItemDTO) {
//                                if (item == null)
//                                    toast("Error: Project Items is NULL")
//                                else {
//                                    infoTextView.visibility = View.VISIBLE
//                                    selectProjectItemdrop.visibility = View.GONE
//                                    mid_lin.visibility = View.VISIBLE
//                                    last_lin.visibility = View.VISIBLE
//                                    selectedProjectitem = item
//                                }
//
////                                infoTextView.setText(i_tems[1].itemCode)
//                            }
//
//                        })
//                })
//
//            } catch (e: ApiException) {
//                toast(e.message!!)
//            } catch (e: NoInternetException) {
//                toast(e.message!!)
//            }
//        }
//    }
//
//    private fun onResetClicked() {
////        setJob(null)
//        resetContractAndProjectSelection()
//    }
//    fun resetContractAndProjectSelection() {
//        items.clear()
//        adapter?.setData(items)
//        descriptionEditText.text!!.clear()
//        mid_lin.visibility = View.GONE
//        last_lin.visibility = View.GONE
//        selectProjectItemdrop.visibility = View.GONE
//        photoLin.visibility = View.GONE
//        selectProjectLayout.visibility = View.VISIBLE
//        setLayoutsVisibility()
//        setMenuItems()
//    }
//
//
//    override fun onResume() {
//        super.onResume()
//        enableBackNavigation()
//        setLayoutsVisibility()
//        setDueDateTextView()
//        setStartDateTextView()
//        calculateTotalCost()
//        setMenuItems()
//        updateTitle()
//    }
//    fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
//        startDateTextView.setText(DateUtil.toStringReadable(year, month, dayOfMonth))
//        startDateCardView.startAnimation(bounce_500)
//        val calendar = Calendar.getInstance()
//        calendar[year, month] = dayOfMonth
//        job?.StartDate = (calendar.time.toString())
//    }
//
//    fun setStartDateTextView() {
//        if (job != null) {
//            val dateString: String = job!!.StartDate!!
//            if (dateString != null) startDateTextView.text = dateString
//        }
//    }
//
//    fun setDueDateTextView() {
//        if (job != null) {
//            val dateString: String = job!!.DueDate!!
//            if (dateString != null) dueDateTextView.text = dateString
//        }
//    }
//
//    fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
//        dueDateTextView.setText(DateUtil.toStringReadable(year, month, dayOfMonth))
//        dueDateCardView.startAnimation(bounce_500)
//        val calendar = Calendar.getInstance()
//        calendar[year, month] = dayOfMonth
//        job?.DueDate = (calendar.time.toString())
//    }
//
//    fun updateTitle() {
//        title = if (isJobSaved) "Saved Job" else "New Job"
//        setResetButton()
//    }
//
//
//
//    private fun saveJob() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//
////    override fun onDestroyView() {
////
////        super.onDestroyView()
////    }
//
//    override fun onStarted() {
//        data_loading.show()
//    }
//
//    override fun onSuccess() {
//      data_loading.hide()
//    }
//
//    override fun onFailure(message: String) {
//       data_loading.hide()
//    }
//
//    override fun onJobSubmitted() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun onInvalidJob() {
//        toastShort("Incomplete estimates!")
//        itemsCardView.startAnimation(shake_long)
//    }
//
//    override fun getJob(): JobDTO {
//        return job!!
//    }
//
//
//
//    override val activity: NewJobActivity?
//        get() = this
//
//    override fun showProgressDialog() {
//        showProgressDialog("")
//    }
//
//
//
//    override fun takePhoto(REQUEST_CODE: Int, itemInfoMap: MutableMap<String, String>?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
//}
