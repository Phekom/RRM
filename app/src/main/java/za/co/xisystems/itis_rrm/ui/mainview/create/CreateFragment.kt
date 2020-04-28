package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_createjob.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.models.CreateViewModel
import za.co.xisystems.itis_rrm.ui.models.CreateViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show
import java.util.*


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
    lateinit var useR: UserDTO
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        try {
            setContract()
        } catch (e: NoInternetException) {
            snackError(e.message)
            Timber.e(e, "No Internet Connection")
        } catch (e: NoConnectivityException) {
            snackError(e.message)
            Timber.e(e, "Backend Host Unreachable")
        }

        return inflater.inflate(R.layout.fragment_createjob, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            val user = createViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                useR = user_
            }
            )
        }

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.selectContractProjectContinueButton -> {
                    val description = descriptionEditText.text!!.toString().trim { it <= ' ' }
                    if (description.isEmpty()) {
                        toast("Please Enter Description")
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

    }

    private fun createNewJob() {
        Coroutines.main {
            val createdJob = createNewJob(
                selectedContract?.contractId,
                selectedProject?.projectId,
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
            today.toString(),
            today.toString(),
            today.toString(),
            null,//null,null,null,null,
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

        toast(createdJob.JobId)
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

                contractData.observe(viewLifecycleOwner, Observer { contractList ->
                    val contractIndices = arrayOfNulls<String>(contractList.size)
                    for (contract in contractList.indices) {
                        contractIndices[contract] = contractList[contract].contractNo
                    }
                    Timber.d("Thread completed.")
                    setSpinner(
                        requireContext().applicationContext,
                        contractSpinner,
                        contractList,
                        contractIndices, //null)
                        object : SpinnerHelper.SelectionListener<ContractDTO> {
                            override fun onItemSelected(position: Int, item: ContractDTO) {
                                if (item == null)
                                    Toast.makeText(
                                        context!!.applicationContext,
                                        "Error: Contract is NULL",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                else {
                                    selectedContract = item
                                    setProjects(item.contractId)
                                    Coroutines.main {
                                        createViewModel.setContractId(item.contractId)
                                    }
                                }
                            }

                        })
                })
            }
        } catch (e: NoInternetException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Timber.e(e, "No Internet Connection")

        } catch (e: NoConnectivityException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Timber.e(e, "Backend Host Unreachable")
        }

    }

    private fun setProjects(contractId: String?) {
        Coroutines.main {
//            try {
            val projects = createViewModel.getSomeProjects(contractId!!)

            projects.observe(viewLifecycleOwner, Observer { projec_t ->
                data_loading.hide()
                val projectNmbr = arrayOfNulls<String>(projec_t.size)
                for (project in projec_t.indices) {
                    projectNmbr[project] = projec_t[project].projectCode
                }
                setSpinner(requireContext().applicationContext,
                    projectSpinner,
                    projec_t,
                    projectNmbr, //null)
                    object : SpinnerHelper.SelectionListener<ProjectDTO> {
                        override fun onItemSelected(position: Int, item: ProjectDTO) {

                            if (item == null)
                                Toast.makeText(
                                    context!!.applicationContext,
                                    "Error: Project is NULL",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else {
                                selectedProject = item
                                Coroutines.main {
                                    createViewModel.setProjectId(item.projectId)
                                }
                            }
                        }
                    })

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











