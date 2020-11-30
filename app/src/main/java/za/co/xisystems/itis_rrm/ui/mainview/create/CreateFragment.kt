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
import kotlinx.android.synthetic.main.fragment_createjob.*
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
import java.util.Calendar

/**
 * Created by Francis Mahlava on 2019/10/18.
 * Updated by Shaun McDonald on 2020/04/22
 */

class CreateFragment : BaseFragment(R.layout.fragment_createjob), OfflineListener, KodeinAware {

    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private val estimatesToRemoveFromDb: ArrayList<JobItemEstimateDTO> =
        ArrayList()

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
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
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
    ): View? {
        return inflater.inflate(R.layout.fragment_createjob, container, false)
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
            user.observe(viewLifecycleOwner, { user_ ->
                useR = user_
            })
        }

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.selectContractProjectContinueButton -> {
                    val description = descriptionEditText.text!!.toString().trim { it <= ' ' }
                    if (description.isEmpty()) {
                        sharpToast(message = "Please Enter Description", style = WARNING)
                        descriptionEditText.startAnimation(shake)
                        //                            return
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

        selectContractProjectContinueButton.setOnClickListener(myClickListener)

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
            val createdJob = createNewJob(
                selectedContract!!.contractId,
                selectedProject!!.projectId,
                useR.userId.toInt(),
                newJobItemEstimatesList,
                jobItemMeasureArrayList,
                jobItemSectionArrayList,
                descri
            )
            createViewModel.saveNewJob(createdJob)
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
        val today = (Calendar.getInstance().time)

        val createdJob = JobDTO(
            0,
            newJobId,
            contractID,
            projectID,
            null,
            0.0,
            0.0,
            description,
            null,
            useR!!,
            null,
            null,
            0,
            0,
            0,
            0,
            DateUtil.DateToString(today),
            DateUtil.DateToString(today),
            DateUtil.DateToString(today),
            null, // null,null,null,null,
            newJobItemEstimatesList,
            jobItemMeasureArrayList,
            jobItemSectionArrayList,
            null,
            0,
            null,
            null,
            null,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            null,
            0,
            0,
            null,
            null,
            null,
            null,
            null,
            0,
            null,
            0,
            null

        )

        newJob = createdJob

        sharpToast(message = "New job created", style = SUCCESS)
        return createdJob
    }

    private fun setContractAndProjectSelection(
        view: View
    ) {
        Navigation.findNavController(view).navigate(R.id.action_nav_create_to_addProjectFragment)
        Coroutines.main {

            createViewModel.setLoggerUser(useR.userId.toInt())
            createViewModel.setContractorNo(selectedContract?.contractNo!!)
            createViewModel.setContractId(selectedContract?.contractId!!)
            createViewModel.setProjectCode(selectedProject?.projectCode!!)
            createViewModel.setProjectId(selectedProject?.projectId!!)
            createViewModel.createNewJob(newJob!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("selectedContract", selectedContract)
        outState.putSerializable("selectedProject", selectedProject)
        outState.putSerializable("job", newJob)
        outState.putSerializable("items", selectedProjectItem)
        outState.putBoolean("isJobSaved", isJobSaved)
        outState.putSerializable("estimatesToRemoveFromDb", estimatesToRemoveFromDb)
        super.onSaveInstanceState(outState)
    }

    private fun setContract() {
        try {
            Coroutines.main {
                data_loading.show()

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
                            contractSpinner,
                            contractList,
                            contractIndices, // null)
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
                    data_loading.hide()
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
            data_loading.hide()
        }
    }

    private fun retryContracts() {
        IndefiniteSnackbar.hide()
        setContract()
    }

    private fun setProjects(contractId: String?) {

        Coroutines.main {
            val projects = createViewModel.getSomeProjects(contractId!!)
            data_loading.show()
            projects.observe(viewLifecycleOwner, { projec_t ->
                val allData = projec_t.count()
                if (projec_t.size == allData) {

                    val projectNmbr = arrayOfNulls<String>(projec_t.size)
                    for (project in projec_t.indices) {
                        projectNmbr[project] = projec_t[project].projectCode
                    }
                    setSpinner(
                        requireContext().applicationContext,
                        projectSpinner,
                        projec_t,
                        projectNmbr, // null)
                        object : SpinnerHelper.SelectionListener<ProjectDTO> {
                            override fun onItemSelected(position: Int, item: ProjectDTO) {
                                selectedProject = item
                                Coroutines.main {
                                    createViewModel.setProjectId(item.projectId)
                                }
                            }
                        })

                    data_loading.hide()
                }
            })
        }
    }

    override fun onStarted() {
        data_loading.show()
    }

    override fun onSuccess() {
        data_loading.hide()
    }

    override fun onFailure(message: String?) {
        data_loading.hide()
    }

    companion object {
        val TAG: String = CreateFragment::class.java.simpleName
    }
}
