package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.fragment_createjob.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IEstimatesAdapter
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.NewJobSelectItemIntentFrag
import za.co.xisystems.itis_rrm.utils.*
import java.util.*


/**
 * Created by Francis Mahlava on 2019/10/18.
 */


class CreateFragment : BaseFragment(R.layout.fragment_createjob), OfflineListener, KodeinAware {
    companion object {
        val TAG: String = CreateFragment::class.java.simpleName
        const val PROJECT_ID1: String = "PROJECT_ID1"
    }


    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()


    var newJobSelectItemIntent: NewJobSelectItemIntentFrag? = null
    private var adapter: IEstimatesAdapter<ProjectItemDTO>? = null
    private val saveMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null
    private var resetMenuItem: MenuItem? = null
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
    //        internal var selectedProject: String? = null
    @MyState
    internal var selectedProjectitem: ProjectItemDTO? = null
//    internal var selectedProjectitem: String? = null

    @MyState
    var new_job: JobDTO? = null

    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    @MyState
    var isJobSaved = false
    private val client: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemSections = ArrayList()
//        the_job = JobDTO()
//        jobItemPhoto = JobItemEstimatesPhotoDTO
        jobItemSectionArrayList = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
        newJobItemEstimatesWorksList = ArrayList()
//        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()

        setHasOptionsMenu(true)

//        jobItemSectionArrayList2 = ArrayList<JobSectionDTO>()
//        jobItemMeasureArrayList2 = ArrayList<JobItemMeasureDTO>()
//        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()
//        newJobItemEstimatesPhotosList2 = ArrayList<JobItemEstimatesPhotoDTO>()
//        newJobItemEstimatesWorksList2 = ArrayList<JobEstimateWorksDTO>()
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
        Log.e("Fragment connection", "No Internet Connection", e)
    } catch (e: NoConnectivityException) {
        snackError(e.message)
        Log.e("Network error", "Backend Host Unreachable", e)
    }
//        navController = this.activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }
        return inflater.inflate(R.layout.fragment_createjob, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            val user = createViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                useR = user_
                  }
            )}

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
                        setContractAndProjectSelection(true, view)

                    }

                }

            }
        }

        selectContractProjectContinueButton.setOnClickListener(myClickListener)

   }

    private fun createNewJob() {
        Coroutines.main {
            val newjob = createNewJob(
                selectedContract?.contractId,
                selectedProject?.projectId,
                useR.userId.toInt(),
                newJobItemEstimatesList,
                jobItemMeasureArrayList,
                jobItemSectionArrayList,
                descri
            )
            createViewModel.saveNewJob(newjob)
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

        val newjob = JobDTO(
            0, newJobId, contractID, projectID, null,
            0.0, 0.0, description, null, useR!!, null, null,
            0, 0, 0, 0, today.toString(), today.toString(), today.toString(), null,//null,null,null,null,
            newJobItemEstimatesList, jobItemMeasureArrayList,jobItemSectionArrayList, null, 0, null, null,
            null, 0, 0, 0, 0, 0, 0, 0, null,
            0, 0, null, null, null,null, null,0,null,0, null

        )

//        val IssueDate = DateUtil.toStringReadable(Calendar.getInstance().time)
        new_job = newjob

        toast(newjob.JobId)
        return newjob
    }

    private fun setContractAndProjectSelection(
        animate: Boolean,
        view: View

    ) {
        Navigation.findNavController(view).navigate(R.id.action_nav_create_to_addProjectFragment)
        Coroutines.main {

//            createViewModel.loggedUser.value = useR.userId.toInt()
//            createViewModel.contract_No.value = selectedContract?.contractNo.toString()
//            createViewModel.contract_ID.value = selectedContract?.contractId
//            createViewModel.project_Code.value = selectedProject?.projectCode.toString()
//            createViewModel.project_ID.value = selectedProject?.projectId
//            createViewModel.descriptioN.value = description
            createViewModel.newjob.value = new_job
        }
    }


    private fun setLayoutsVisibility() {
        val hasItems = adapter != null && adapter!!.itemCount > 0
//        infoTextView.visibility = if (hasItems) View.GONE else View.VISIBLE
//        submitButton.visibility = if (hasItems) View.VISIBLE else View.GONE
//        dueDateCardView.visibility = if (hasItems) View.VISIBLE else View.GONE
//        startDateCardView.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("selectedContract", selectedContract)
        outState.putSerializable("selectedProject", selectedProject)
        outState.putSerializable("job", new_job)
        outState.putSerializable("items", selectedProjectitem)
        outState.putBoolean("isJobSaved", isJobSaved)
        outState.putSerializable("estimatesToRemoveFromDb", estimatesToRemoveFromDb)
        super.onSaveInstanceState(outState)
    }

    private fun setContract() {
        try {
            Coroutines.main {
                data_loading.show()

                val contracts = createViewModel.getContracts()

                contracts.observe(viewLifecycleOwner, Observer { contrac_t ->
                    val contractNmbr = arrayOfNulls<String>(contrac_t.size)
                    for (contract in contrac_t.indices) {
                        contractNmbr[contract] = contrac_t[contract].contractNo
                    }
                    Log.d(TAG, "Thread is Finished ")
                    setSpinner(context!!.applicationContext,
                        contractSpinner,
                        contrac_t,
                        contractNmbr, //null)
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
                                        createViewModel.proId.value = item.contractId
                                    }
                                }
                            }

                        })
                })
            }
        } catch (e: NoInternetException) {
           Toast.makeText(context, e.message,Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No Internet Connection", e)

        } catch (e: NoConnectivityException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Backend Host Unreachable", e)
        }

    }

    private fun setProjects(contractId: String?) {
        Coroutines.main {
//            try {
                val projects = createViewModel.getSomeProjects(contractId!!)
//                val projects = authViewModel.projects.await()
                projects.observe(viewLifecycleOwner, Observer { projec_t ->
                    data_loading.hide()
                    val projectNmbr = arrayOfNulls<String>(projec_t.size)
                    for (project in projec_t.indices) {
                        projectNmbr[project] = projec_t[project].projectCode
                    }
                    setSpinner(context!!.applicationContext,
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
                                        createViewModel.proId.value = item.projectId
                                    }
//                                    selectedProjectTextView.setText(selectedProject?.projectCode)
                                }
                            }
                        })

                })


//            } catch (e: ApiException) {
//                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
//            } catch (e: NoInternetException) {
//                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
//            }
        }

    }

    override fun onStarted() {
        data_loading.show()
//        setContract()
    }

    override fun onSuccess() {
        data_loading.hide()
    }

    override fun onFailure(message: String?) {
        data_loading.hide()
    }

}











