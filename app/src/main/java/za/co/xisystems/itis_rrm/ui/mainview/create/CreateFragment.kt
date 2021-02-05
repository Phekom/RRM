/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 11:31 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.databinding.FragmentCreatejobBinding
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.SUCCESS
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.WARNING
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show
import java.util.ArrayList
import java.util.Date

/**
 * Created by Francis Mahlava on 2019/10/18.
 * Updated by Shaun McDonald on 2020/04/22
 */

class CreateFragment : BaseFragment(), OfflineListener, KodeinAware {

    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private var jobPk: Long = -1L
    private val factory: CreateViewModelFactory by instance()
    private val estimatesToRemoveFromDb: ArrayList<JobItemEstimateDTO> =
        ArrayList()

    // viewBinding implementation
    private var _ui: FragmentCreatejobBinding? = null
    private val ui get() = _ui!!

    @MyState
    var items: ArrayList<ProjectItemDTO> = ArrayList()

    @MyState
    internal var selectedContract: ContractDTO? = null
    private lateinit var useR: UserDTO
    private var descri: String? = null

    @MyState
    internal var selectedProject: ProjectDTO? = null

    @MyState
    internal var selectedProjectItem: ProjectItemDTO? = null

    @MyState
    var newJob: JobDTO? = null
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    @MyState
    var isJobSaved = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemSections = ArrayList()
        jobItemSectionArrayList = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
        newJobItemEstimatesWorksList = ArrayList()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) item2.isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentCreatejobBinding.inflate(inflater, container, false)
        return ui.root
    }

    init {
        // setup for CreateFragment
        lifecycleScope.launch {
            whenStarted {
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            val user = createViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                useR = userDTO
            })
        }

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.selectContractProjectContinueButton -> {
                    val description = ui.descriptionEditText.text!!.toString().trim { it <= ' ' }
                    if (description.isEmpty()) {
                        sharpToast(message = "Please Enter Description", style = WARNING)
                        ui.descriptionEditText.startAnimation(shake)
                    } else {
                        activity?.hideKeyboard()
                        createNewJob()
                        descri = description
                        createViewModel.setDescription(descri!!)
                        setContractAndProjectSelection(view)
                    }
                }
            }
        }

        ui.selectContractProjectContinueButton.setOnClickListener(myClickListener)

        try {
            setContract()
        } catch (t: Throwable) {
            val contractErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                view = this.requireView(),
                throwable = contractErr,
                refreshAction = { retryContracts() }
            )
        }
    }

    private fun createNewJob() {
        Coroutines.main {
            newJob = createNewJob(
                selectedContract!!.contractId,
                selectedProject!!.projectId,
                useR.userId.toInt(),
                newJobItemEstimatesList,
                jobItemMeasureArrayList,
                jobItemSectionArrayList,
                descri
            )
            createViewModel.backupJob(newJob!!)
            createViewModel.jobId.observe(viewLifecycleOwner, {
                Timber.d("Job PK: $it")
            })
        }
    }

    private fun createNewJob(
        contractID: String?,
        projectID: String?,
        useR: Int?,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>,
        description: String?
    ): JobDTO {
        val newJobId: String = SqlLitUtils.generateUuid()
        val today = Date()

        val createdJob = JobDTO(
            actId = 0,
            jobId = newJobId,
            contractVoId = contractID,
            projectId = projectID,
            sectionId = null,
            startKm = 0.0,
            endKm = 0.0,
            descr = description,
            jiNo = null,
            userId = useR!!,
            trackRouteId = null,
            section = null,
            cpa = 0,
            dayWork = 0,
            contractorId = 0,
            m9100 = 0,
            issueDate = DateUtil.dateToString(today),
            startDate = DateUtil.dateToString(today),
            dueDate = DateUtil.dateToString(today),
            approvalDate = null,
            jobItemEstimates = newJobItemEstimatesList,
            jobItemMeasures = jobItemMeasureArrayList,
            jobSections = jobItemSectionArrayList,
            perfitemGroupId = null,
            recordVersion = 0,
            remarks = null,
            route = null,
            rrmJiNo = null,
            engineerId = 0,
            entireRoute = 0,
            isExtraWork = 0,
            jobCategoryId = 0,
            jobDirectionId = 0,
            jobPositionId = 0,
            jobStatusId = 0,
            projectVoId = null,
            qtyUpdateAllowed = 0,
            recordSynchStateId = 0,
            voId = null,
            workCompleteDate = null,
            workStartDate = null,
            estimatesActId = null,
            measureActId = null,
            worksActId = 0,
            sortString = null,
            activityId = 0,
            isSynced = null

        )
        sharpToast(message = "New job created", style = SUCCESS)
        return createdJob
    }

    private fun setContractAndProjectSelection(
        view: View
    ) {
        Coroutines.main {

            createViewModel.setLoggerUser(useR.userId.toInt())
            createViewModel.setContractorNo(selectedContract?.contractNo!!)
            createViewModel.setContractId(selectedContract?.contractId!!)
            createViewModel.setProjectCode(selectedProject?.projectCode!!)
            createViewModel.setProjectId(selectedProject?.projectId!!)

            // Uniform entry point to minimize confusion - Shaun McDonald
            newJob?.let {
                createViewModel.backupJob(it)
                createViewModel.setJobToEdit(it.jobId)

                val navDirection = CreateFragmentDirections
                    .actionNavCreateToAddProjectFragment(
                        projectId = it.projectId!!,
                        jobId = it.jobId
                    )
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("selectedContractId", selectedContract?.contractId)
        outState.putSerializable("selectedProjectId", selectedProject?.projectId)
        outState.putSerializable("jobId", newJob?.jobId)
        outState.putBoolean("isJobSaved", isJobSaved)
        // outState.putSerializable("estimatesToRemoveFromDb", estimatesToRemoveFromDb)
        super.onSaveInstanceState(outState)
    }

    private fun setContract() {
        try {
            Coroutines.main {
                ui.dataLoading.show()

                val contractData = createViewModel.getContracts()

                contractData.observe(viewLifecycleOwner, { contractList ->
                    val allData = contractList.count()
                    if (contractList.size == allData) {
                        val contractIndices = arrayOfNulls<String>(contractList.size)
                        for (contract in contractList.indices) {
                            contractIndices[contract] = contractList[contract].contractNo
                        }

                        Timber.d("Thread completed.")
                        setSpinner(
                            requireContext().applicationContext,
                            ui.contractSpinner,
                            contractList,
                            contractIndices,
                            object : SpinnerHelper.SelectionListener<ContractDTO> {
                                override fun onItemSelected(position: Int, item: ContractDTO) {
                                    selectedContract = item
                                    setProjects(item.contractId)
                                    Coroutines.main {
                                        createViewModel.setContractId(item.contractId)
                                    }
                                }
                            })
                    }
                    ui.dataLoading.hide()
                })
            }
        } catch (t: Throwable) {
            val contractErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                this.requireView(),
                contractErr,
                refreshAction = { retryContracts() }
            )
        } finally {
            ui.dataLoading.hide()
        }
    }

    private fun retryContracts() {
        IndefiniteSnackbar.hide()
        setContract()
    }

    private fun setProjects(contractId: String?) {

        Coroutines.main {
            val projects = createViewModel.getSomeProjects(contractId!!)
            ui.dataLoading.show()
            projects.observe(viewLifecycleOwner, { projectList ->
                val allData = projectList.count()
                if (projectList.size == allData) {

                    val projectNmbr = arrayOfNulls<String>(projectList.size)
                    for (project in projectList.indices) {
                        projectNmbr[project] = projectList[project].projectCode
                    }
                    setSpinner(
                        requireContext().applicationContext,
                        ui.projectSpinner,
                        projectList,
                        projectNmbr, // null)
                        object : SpinnerHelper.SelectionListener<ProjectDTO> {
                            override fun onItemSelected(position: Int, item: ProjectDTO) {
                                selectedProject = item
                                Coroutines.main {
                                    createViewModel.setProjectId(item.projectId)
                                }
                            }
                        })

                    ui.dataLoading.hide()
                }
            })
        }
    }

    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // prevent viewBinding from leaking
        _ui = null
    }

    override fun onStarted() {
        ui.dataLoading.show()
    }

    override fun onSuccess() {
        ui.dataLoading.hide()
    }

    override fun onFailure(message: String?) {
        ui.dataLoading.hide()
    }

    companion object {
        val TAG: String = CreateFragment::class.java.simpleName
    }
}
