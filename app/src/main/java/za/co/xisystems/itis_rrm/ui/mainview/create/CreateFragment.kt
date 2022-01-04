/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MobileNavigationDirections
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.databinding.FragmentCreatejobBinding
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show
import java.util.ArrayList
import java.util.Date

/**
 * Created by Francis Mahlava on 2019/10/18.
 * Updated by Shaun McDonald on 2020/04/22
 */

class CreateFragment : BaseFragment(), OfflineListener, DIAware {

    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()

    // viewBinding implementation
    private var _ui: FragmentCreatejobBinding? = null
    private val ui get() = _ui!!

    var items: ArrayList<ProjectItemDTO> = ArrayList()

    internal var selectedContract: ContractSelector? = null
    private lateinit var useR: UserDTO
    private var descri: String? = null

    internal var selectedProject: ProjectSelector? = null

    private var newJob: JobDTO? = null
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemSections = ArrayList()
        jobItemSectionArrayList = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
        newJobItemEstimatesWorksList = ArrayList()
        createViewModel =
            ViewModelProvider(this.requireActivity(), factory)[CreateViewModel::class.java]

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Coroutines.main {
            val user = createViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                useR = userDTO
            })
        }

        val myClickListener = View.OnClickListener { clickedView ->
            when (clickedView) {
                ui.selectContractProjectContinueButton -> {
                    val description = ui.descriptionEditText.text!!.toString().trim { it <= ' ' }
                    if (description.isEmpty()) {
                        extensionToast(message = "Please Enter Description", style = ToastStyle.WARNING)
                        ui.descriptionEditText.startAnimation(shake)
                    } else {
                        activity?.hideKeyboard()
                        createNewJob()
                        descri = description
                        createViewModel.setDescription(descri!!)
                        setContractAndProjectSelection(clickedView)
                    }
                }
            }
        }

        ui.selectContractProjectContinueButton.setOnClickListener(myClickListener)

        try {
            setContract()
        } catch (t: Throwable) {
            val contractErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = contractErr,
                refreshAction = { this.retryContracts() }
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
        extensionToast(message = "New job created", style = ToastStyle.SUCCESS)
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
        outState.putString(CONTRACT_KEY, selectedContract?.contractId)
        outState.putString(PROJECT_KEY, selectedProject?.projectId)
        outState.putString(JOB_KEY, newJob?.jobId)
        super.onSaveInstanceState(outState)
    }

    private fun setContract() {
        try {
            Coroutines.main {
                ui.dataLoading.show()

                val contractData = createViewModel.getContractSelectors()

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
                            object : SpinnerHelper.SelectionListener<ContractSelector> {
                                override fun onItemSelected(position: Int, item: ContractSelector) {
                                    selectedContract = item
                                    setProjects(item.contractId)
                                    Coroutines.main {
                                        createViewModel.setContractId(item.contractId)
                                    }
                                }
                            }
                        )
                    }
                    ui.dataLoading.hide()
                })
            }
        } catch (t: Throwable) {
            val contractErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = contractErr,
                refreshAction = { this.retryContracts() }
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
            val projects = createViewModel.getProjectSelectors(contractId!!)
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
                        object : SpinnerHelper.SelectionListener<ProjectSelector> {
                            override fun onItemSelected(position: Int, item: ProjectSelector) {
                                selectedProject = item
                                Coroutines.main {
                                    createViewModel.setProjectId(item.projectId)
                                }
                            }
                        }
                    )

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
        const val CONTRACT_KEY = "contractId"
        const val PROJECT_KEY = "projectId"
        const val JOB_KEY = "jobId"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@CreateFragment.findNavController().navigate(MobileNavigationDirections.actionGlobalNavHome())
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }
}
