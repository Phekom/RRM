package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.create

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.databinding.FragmentCreationHomeBinding
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ContractVoSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.domain.ProjectVoSelector
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.utils.*
import java.util.*


class CreationHomeFragment : BaseFragment(), OfflineListener {


    private lateinit var creationViewModel: CreationViewModel
    private val factory: CreationViewModelFactory by instance()
    private var _ui: FragmentCreationHomeBinding? = null
    private val ui get() = _ui!!

    var items: ArrayList<ProjectItemDTO> = ArrayList()

    internal var selectedContract: ContractSelector? = null
    private lateinit var useR: UserDTO
    internal var selectedProject: ProjectSelector? = null
    internal var selectedContractVoNmbr: ContractVoSelector? = null
    private var newJob: JobDTO? = null
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>
    private var networkEnabled: Boolean = false
    private var selectedVoJobYesNo: String? = null

    private val backClickListener = View.OnClickListener {
        setBackPressed()
    }

    init {
        lifecycleScope.launch {
            whenCreated {
                acquireUser()
            }
            whenStarted {
                requireActivity().hideKeyboard()
                acquireUser()
            }
            whenResumed {
                acquireUser()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemSections = ArrayList()
        jobItemSectionArrayList = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
        newJobItemEstimatesWorksList = ArrayList()
        ping()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var jOB_ACTIVITY: JobCreationActivity = context as JobCreationActivity
        jOB_ACTIVITY.navigationView.visibility = View.VISIBLE
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent(requireContext(), MainActivity::class.java).also { home ->
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _ui = FragmentCreationHomeBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        creationViewModel =
            ViewModelProvider(this, factory)[CreationViewModel::class.java]
        Coroutines.main {
             try {
                 _ui?.descriptionEditText?.filters = arrayOf(ValidateInputs.EMOJI_FILTER)
                 _ui?.createJobBtn?.setOnClickListener { view ->
                     Coroutines.main {
                         var description = ""
                         if (!ValidateInputs.isValidInputText(_ui?.descriptionEditText?.text.toString())) {
                             _ui?.descriptionEditText?.error = getString(R.string.invalid_char)+ " Or ( $&+~;=\\?@|/'<>^*()%!- )"
                         } else {
                             description = _ui?.descriptionEditText?.text!!.toString()//.trim { it <= ' ' }
                         }

                         if (description.isEmpty()) {
                             extensionToast(message = "Please Enter Description", style = ToastStyle.WARNING)
                             _ui?.descriptionEditText?.startAnimation(BaseFragment.shake)
                         } else if (selectedVoJobYesNo == null) {
                             extensionToast(message = "Please Indicate If It's a Variation Job Or Not", style = ToastStyle.WARNING)
                             _ui?.voJobLin?.startAnimation(BaseFragment.shake)
                         } else {
                             activity?.hideKeyboard()
                             if (selectedVoJobYesNo == getString(R.string.yes)){
                                 Coroutines.main {
                                     newJob = createNewJob(
                                         selectedContract!!.contractId,
                                         selectedProject!!.projectId,
                                         useR.userId.toInt(),
                                         newJobItemEstimatesList,
                                         jobItemMeasureArrayList,
                                         jobItemSectionArrayList,
                                         description,
                                         selectedContractVoNmbr?.contractVoId,
                                     )
                                     creationViewModel.backupJob(newJob!!)
                                     setContractAndProjectSelection(newJob, view)
                                 }
                             }else{
                                 newJob = createNewJob(
                                     selectedContract!!.contractId,
                                     selectedProject!!.projectId,
                                     useR.userId.toInt(),
                                     newJobItemEstimatesList,
                                     jobItemMeasureArrayList,
                                     jobItemSectionArrayList,
                                     description,
                                     null
                                 )
                                 creationViewModel.backupJob(newJob!!)
                                 setContractAndProjectSelection(newJob, view)
                             }
                         }
                     }
                 }

                 variationOrderJob()
                 setContract()

            } catch (t: Throwable) {
                val errorMessage = "Failed to download remote data: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, errorMessage)
                crashGuard(
                    throwable = XIResult.Error(t, errorMessage),
                    refreshAction = { this@CreationHomeFragment.retrySync() }
                )
            }

        }


    }

    private fun ping() {
        uiScope.launch(uiScope.coroutineContext) {
            try {
                if (networkEnabled) {
                    acquireUser()
                }
            } catch (t: Throwable) {
                val pingEx = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    throwable = pingEx,
                    refreshAction = { this@CreationHomeFragment.retrySync() }
                )
            }
        }
    }

    private fun retrySync() {
        IndefiniteSnackbar.hide()
    }

    private suspend fun acquireUser() {
        try {
            val userJob = uiScope.launch(uiScope.coroutineContext) {
                val user = creationViewModel.user.await()
                user.observe(viewLifecycleOwner, { userInstance ->
                    userInstance.let {
                        useR = it
                        checkConnectivity()
                        if (networkEnabled) {
                            servicesHealthCheck()
                        }
                    }
                })
            }
            userJob.join()
        } catch (t: Throwable) {
            val errorMessage = "Failed to load user: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, errorMessage)
            val connectErr = XIResult.Error(t, errorMessage)
            crashGuard(
                throwable = connectErr,
                refreshAction = { this.retryAcquireUser() }
            )
        }
    }

    private fun servicesHealthCheck() = uiScope.launch(uiScope.coroutineContext) {
        if (!activity?.isFinishing!! && useR != null && networkEnabled) {
            creationViewModel.healthState.observe(viewLifecycleOwner, { outcome ->
                outcome.getContentIfNotHandled()?.let { result ->
                    processHealthCheck(result)
                }
            })
            creationViewModel.healthCheck()
        }
    }

    private fun retryAcquireUser() {
        IndefiniteSnackbar.hide()
        uiScope.launch(uiScope.coroutineContext) {
            checkConnectivity()
            if (networkEnabled) {
                acquireUser()
            }
        }
    }

    private fun checkConnectivity(): Boolean {
        var result = true
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = this.requireContext().isConnected
        return result
    }

    private fun retryHealthCheck() {
        IndefiniteSnackbar.hide()
        servicesHealthCheck()
    }

    private fun processHealthCheck(result: XIResult<Boolean>) {
        when (result) {
            is XIResult.Success<Boolean> -> {

                //updateServiceHealth(result.data)
            }
            is XIResult.Error -> {
                crashGuard(
                    throwable = result,
                    refreshAction = { this@CreationHomeFragment.retryHealthCheck() }
                )
            }
            else -> {
                Timber.d("$result")
            }
        }
    }

    private fun createNewJob(
        contractID: String?,
        projectID: String?,
        useR: Int?,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>,
        description: String?,
        contractVoId: String?
    ): JobDTO {
        val newJobId: String = SqlLitUtils.generateUuid()
        val today = Date()

        val createdJob = JobDTO(
            actId = 0,
            jobId = newJobId,
            contractId = contractID,
            contractVoId = contractVoId,
            projectId = projectID,
            projectVoId = null,
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
            qtyUpdateAllowed = 0,
            recordSynchStateId = 0,
            workCompleteDate = null,
            workStartDate = null,
            estimatesActId = null,
            measureActId = null,
            worksActId = 0,
            sortString = null,
            activityId = 0,
            isSynced = null,
            voJob = selectedVoJobYesNo
        )
        extensionToast(message = "New job created", style = ToastStyle.SUCCESS)
        return createdJob
    }

    private fun setContractAndProjectSelection(
        newJob: JobDTO?,
        view: View
    ) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = CreationHomeFragmentDirections
                    .actionNavigationCreateToNavigationAddItems(newJob?.projectId!!, newJob.jobId, newJob.contractVoId)
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
                _ui?.dataLoading?.show()
                val contractData = creationViewModel.getContractSelectors()
                contractData.observe(this, Observer { contractList ->
                    val allData = contractList.count()
                    if (contractList.size == allData) {
                        val contractIndices = arrayOfNulls<String>(contractList.size)
                        for (contract in contractList.indices) {
                            contractIndices[contract] = contractList[contract].contractNo
                        }
                        Timber.d("Thread completed.")
                        SpinnerHelper.setSpinner(
                            requireContext().applicationContext,
                            _ui?.contractSpinner!!,
                            contractList,
                            contractIndices,
                            object : SpinnerHelper.SelectionListener<ContractSelector> {
                                override fun onItemSelected(position: Int, item: ContractSelector) {
                                       selectedContract = item
                                       setProjects(item.contractId)
                                }
                            }
                        )
                    }
                    _ui?.dataLoading?.hide()
                })
            }
        } catch (t: Throwable) {
            val contractErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = contractErr,
                refreshAction = { this.retryContracts() }
            )
        } finally {
            _ui?.dataLoading?.hide()
        }
    }

    private fun retryContracts() {
        IndefiniteSnackbar.hide()
        variationOrderJob()
        setContract()
    }

    private fun variationOrderJob() {
        Coroutines.main {
            when {
                newJob?.voJob.equals("Yes") -> {
                    _ui?.voJobYes?.isChecked = true
                    selectedVoJobYesNo = getString(R.string.yes)
                    ui.voJobSelectLin.visibility = View.VISIBLE
                }
                newJob?.voJob.equals("No") -> {
                    _ui?.voJobNo?.isChecked = true
                    selectedVoJobYesNo = getString(R.string.no)
                    ui.voJobSelectLin.visibility = View.GONE
                }
                else -> {
                    _ui?.voJobYes?.isChecked = false
                    _ui?.voJobNo?.isChecked = false
                    ui.voJobSelectLin.visibility = View.GONE
                }
            }

            _ui?.voJobRadioGroup?.setOnCheckedChangeListener { _, checkedId ->
                val radio: RadioButton = ui.root.findViewById(checkedId)
                selectedVoJobYesNo = "${radio.text}"
                if (radio.text.equals(getString(R.string.yes))){
                    ui.voJobSelectLin.visibility = View.VISIBLE
                }else{ui.voJobSelectLin.visibility = View.GONE}
            }
        }
    }
    
    private fun setProjects(contractId: String?) {
        Coroutines.main {
            val projects = creationViewModel.getProjectSelectors(contractId!!)
            _ui?.dataLoading?.show()
            projects.observe(viewLifecycleOwner, { projectList ->
                val allData = projectList.count()
                if (projectList.size == allData) {

                    val projectNmbr = arrayOfNulls<String>(projectList.size)
                    for (project in projectList.indices) {
                        projectNmbr[project] = projectList[project].projectCode
                    }
                    SpinnerHelper.setSpinner(
                        requireContext().applicationContext,
                        _ui?.projectSpinner!!,
                        projectList,
                        projectNmbr, // null)
                        object : SpinnerHelper.SelectionListener<ProjectSelector> {
                            override fun onItemSelected(position: Int, item: ProjectSelector) {
                                Coroutines.main {
                                    selectedProject = item
                                    val projectVoData = creationViewModel.getProjectVoData(item.projectId)
                                    projectVoData.observe(requireActivity(), Observer { projectVoDta ->
                                        if(projectVoDta.isEmpty()){
                                            ui.voJobLin.visibility = View.GONE
                                            ui.voJobSelectLin.visibility = View.GONE
                                            selectedVoJobYesNo = getString(R.string.no)
                                        }else{
                                            ui.voJobLin.visibility = View.VISIBLE
                                            setContractVoNumber(item.contractId)
                                        }
                                    })
                                }

                            }
                        }
                    )

                    _ui?.dataLoading?.hide()
                }
            })
        }
    }

    private fun setContractVoNumber(contractId: String) {
        Coroutines.main {
                val contractVoNumbers = creationViewModel.getContractVoSelectors(contractId)
                _ui?.dataLoading?.show()
                contractVoNumbers.observe(viewLifecycleOwner, { projectVoList ->
                    //val newList = projectVoList.distinctBy { it.voNumber }
                    val allData = projectVoList.count()
                    if (projectVoList.size == allData) {

                        val projectVoNmbr = arrayOfNulls<String>(projectVoList.size)
                        for (project in projectVoList.indices) {
                            projectVoNmbr[project] = projectVoList[project].voNumber
                        }
                        SpinnerHelper.setSpinner(
                            requireContext().applicationContext,
                            _ui?.contractVoSpinner!!,
                            projectVoList,
                            projectVoNmbr, // null)
                            object : SpinnerHelper.SelectionListener<ContractVoSelector> {
                                override fun onItemSelected(position: Int, item: ContractVoSelector) {
                                    Coroutines.main {
                                        selectedContractVoNmbr = item
                                    }

                                }
                            }
                        )

                        _ui?.dataLoading?.hide()
                    }
                })
        }
    }

    private fun setBackPressed() {
        Intent(requireContext(), MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _ui = null
    }

    override fun onStarted() {
        _ui?.dataLoading?.show()
    }

    override fun onSuccess() {
        _ui?.dataLoading?.hide()
    }

    override fun onFailure(message: String?) {
        _ui?.dataLoading?.hide()
    }

    companion object {
        val TAG: String = CreationHomeFragment::class.java.simpleName
        const val CONTRACT_KEY = "contractId"
        const val PROJECT_KEY = "projectId"
        const val JOB_KEY = "jobId"
    }


}