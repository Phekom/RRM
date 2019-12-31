package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.IJobSubmit
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IEstimatesAdapter
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.NewJobSelectItemIntentFrag
import za.co.xisystems.itis_rrm.utils.*
import java.util.*


class CreateFragment : BaseFragment(), OfflineListener , KodeinAware, IJobSubmit {
    companion object {
        val TAG: String = CreateFragment::class.java.simpleName
        val PROJECT_ID1 : String  = "PROJECT_ID1"
    }


    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()


    var newJobSelectItemIntent: NewJobSelectItemIntentFrag? = null
    private var adapter: IEstimatesAdapter<ItemDTO>? = null
    private val saveMenuItem: MenuItem? = null
    private var deleteMenuItem: MenuItem? = null
    private var resetMenuItem: MenuItem? = null
    private val estimatesToRemoveFromDb: ArrayList<JobItemEstimateDTO> =
        ArrayList<JobItemEstimateDTO>()
    @MyState
    var items: ArrayList<ItemDTO> = ArrayList<ItemDTO>()
    @MyState
    internal var selectedContract: ContractDTO? = null
    //        internal var selectedContract: String? = null
    @MyState
    internal var selectedProject: ProjectDTO? = null
    //        internal var selectedProject: String? = null
    @MyState
    internal var selectedProjectitem: ItemDTO? = null
//    internal var selectedProjectitem: String? = null

    @MyState
    internal var job: JobDTO? = null
    @MyState
    var isJobSaved = false
    private val client: GoogleApiClient? = null




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setContract()
//        navController = this.activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }
        return inflater.inflate(R.layout.fragment_createjob, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        createViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

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
                        val job = job
                        job?.issueDate = (Calendar.getInstance().time).toString()
                        job?.descr = description
//                        setJob(job!!)
                        setContractAndProjectSelection(true, view)

                    }

                }

            }
        }

        selectContractProjectContinueButton.setOnClickListener(myClickListener)




//        item_recyclerView.layoutManager = LinearLayoutManager(context?.applicationContext)
//        adapter = EstimatesAdapter(context!!.applicationContext, items, AbstractAdapter.OnItemClickListener<ItemDTO?> {
//           fun onItemClick(item: ItemDTO?) {
////                            newJobEditEstimateIntent.startActivityForResult(this, job, item)
//                        }
//        }
//
//        )
//        item_recyclerView.adapter = adapter!!.adapter

    }















    private fun setJob(job: JobDTO) {
        this.job = job
    }

    private fun setContractAndProjectSelection(animate: Boolean, view: View) {
        Navigation.findNavController(view).navigate(R.id.action_nav_create_to_addProjectFragment)

//        if (selectedContract != null && selectedProject != null && job != null) {
//        if (selectedContract != null && selectedProject != null) {
//            selectProjectLayout.visibility = View.GONE
//            mid_lin.visibility = View.VISIBLE
//            if (animate) {
//                addItemLayout.startAnimation(bounce_750)
//                itemsCardView.startAnimation(bounce_500)
//            }
//
        Coroutines.main {
            createViewModel.contract_No.value = selectedContract?.contractNo.toString()
            createViewModel.project_Code.value = selectedProject?.projectCode.toString()

        }
//            selectedContractTextView.text = selectedContract?.contractNo.toString()
//            selectedProjectTextView.text = selectedProject?.projectCode.toString()
//            job?.contractVoId = selectedContract!!.contractId
//            job?.projectId = selectedProject!!.projectId
//            setMenuItems()
//        }
    }

    fun setMenuItems() {
        if (saveMenuItem != null) {
            saveMenuItem.setVisible(job != null && items != null && !items.isEmpty())
        }
        setResetButton()
    }

    private fun setResetButton() {
//        if (isJobSaved) resetButton.visibility = View.GONE else resetButton.visibility =
//            View.VISIBLE
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
        outState.putSerializable("job", job)
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
//                val contracts = authViewModel.offlinedata.await()
                contracts.observe(viewLifecycleOwner, Observer { contrac_t ->
                    val contractId = contrac_t
                    val contractNmbr = arrayOfNulls<String>(contrac_t.size)
                    for (contract in contrac_t.indices) {
                        contractNmbr[contract] = contrac_t[contract].contractNo
                    }
                    Log.d(TAG, "Thread is Finished ")
                    setSpinner(context!!.applicationContext,
                        contractSpinner,
                        contractId,
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
                                }
                            }

                        })
                })
            }
        } catch (e: ApiException) {
            toast(e.message)
        } catch (e: NoInternetException) {
            toast(e.message)
            Log.e("NetworkConnection", "No Internet Connection", e)

        }

    }

    private fun setProjects(contractId: String?) {
        Coroutines.main {
//            try {
                val projects = createViewModel.getSomeProjects(contractId!!)
//                val projects = authViewModel.projects.await()
                projects.observe(viewLifecycleOwner, Observer { projec_t ->
                    data_loading.hide()
                    val projects = projec_t
                    val projectNmbr = arrayOfNulls<String>(projec_t.size)
                    for (project in projec_t.indices) {
                        projectNmbr[project] = projec_t[project].projectCode
                    }
                    setSpinner(context!!.applicationContext,
                        projectSpinner,
                        projects,
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


    override fun onJobSubmitted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInvalidJob() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStarted() {
        data_loading.show()
//        setContract()
    }

    override fun onSuccess() {
        data_loading.hide()
    }

    override fun onFailure(message: String) {
        data_loading.hide()
    }

}











