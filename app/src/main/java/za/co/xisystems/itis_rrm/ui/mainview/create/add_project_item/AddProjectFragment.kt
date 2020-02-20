package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_add_project_items.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SwipeTouchCallback
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.*


/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class AddProjectFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private val myfactory: UnSubmittedViewModelFactory by instance()
    private val factory: CreateViewModelFactory by instance()
    private var dueDateDialog: DatePickerDialog? = null
    private var startDateDialog: DatePickerDialog? = null
    private lateinit var jobDataController : JobDataController
    private val rainbow200: IntArray by lazy { resources.getIntArray(R.array.rainbow_200) }
    private val swipeSection = Section()
    private var useR: Int? = null
    var contractID : String? = null
    var projectID : String? = null
    private lateinit var groupAdapter : GroupAdapter<GroupieViewHolder>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private val startDate: Date? = null
    private val dueDate: Date? = null
    @MyState
    private var job: JobDTO? = null
    @MyState
//    private lateinit var items: MutableList<ItemDTO>
    private var items: List<ItemDTOTemp> = ArrayList<ItemDTOTemp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        groupAdapter = GroupAdapter<GroupieViewHolder>()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
        jobDataController  =  JobDataController
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
    }//                        createViewModel.delete(item)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        calculateTotalCost()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_project_items, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        createViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")as Throwable
        unsubmittedViewModel = activity?.run {
            ViewModelProviders.of(this, myfactory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        last_lin.visibility = View.GONE
        totalCostTextView.visibility = View.GONE
        dueDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
        startDateTextView.text = DateUtil.toStringReadable(DateUtil.currentDateTime)
//        items = getSerializable("items") as ArrayList<ItemDTO>
        createViewModel.project_Code.observe(viewLifecycleOwner, Observer { pro_code ->
            selectedProjectTextView.text = pro_code
        })
        createViewModel.contract_No.observe(viewLifecycleOwner, Observer { contrct ->
            selectedContractTextView.text = contrct
        })
        createViewModel.newjob.observe(viewLifecycleOwner, Observer { newJ ->
//            toast(newJ.JobId + "/n" + newJ.ProjectId )
            projectID = newJ.ProjectId
            job = newJ
        })


        createViewModel.loggedUser.observe(viewLifecycleOwner, Observer { user ->
            useR = user
//            selectedContractTextView.text = user
        })

//        createViewModel.contract_ID.observe(viewLifecycleOwner, Observer { contrct_id ->
//            toast(contrct_id)
//            contractID = contrct_id
//        })
        unsubmittedViewModel.jobtoEdit_Item.observe(viewLifecycleOwner, Observer { jobToEdit ->
                        toast( jobToEdit.Descr )
            Coroutines.main {
                projectID = jobToEdit.ProjectId
                job = jobToEdit
                val contrct = createViewModel.getContractNoForId(jobToEdit.ContractVoId)
                val pro_code = createViewModel.getProjectCodeForId(jobToEdit.ProjectId)
                selectedContractTextView.text = contrct
                selectedProjectTextView.text = pro_code

                infoTextView.visibility = View.GONE
                last_lin.visibility = View.VISIBLE
                totalCostTextView.visibility = View.VISIBLE

                createViewModel.jobtoEdit_Item.value = jobToEdit
                calculateTotalCost()
            }

        })



//        createViewModel.project_ID.observe(viewLifecycleOwner, Observer { projec_id ->
//                        toast(projec_id)
//            projectID = projec_id
//        })



        setmyClickListener()


        createViewModel.Sec_Item.observe(viewLifecycleOwner, Observer { p_Item ->
            infoTextView.visibility = View.GONE
            last_lin.visibility = View.VISIBLE
            totalCostTextView.visibility = View.VISIBLE
//            if (pro_Item.itemDTO.itemId != null)
//            if (pro_Item.itemDTO.itemId == items.get(0).itemId ){
//                toast("Item already added!")
//            }else{

//            }
//            assert(items.contains(pro_Item.itemDTO))

            Coroutines.main {
                val projecItems = createViewModel.getAllProjecItems(projectID!!)
                projecItems.observe(viewLifecycleOwner, Observer { pro_Items ->
                    if (pro_Items.isEmpty()){

                        totalCostTextView.text = ""
                        last_lin.visibility = View.GONE
                        totalCostTextView.visibility = View.GONE
                    }
                    items = pro_Items
                    for (item in pro_Items.listIterator()) {
                        if (job?.JobId != item.jobId){
                            groupAdapter.clear()
                            totalCostTextView.clearComposingText()
                        }else{
                            // initRecyclerView(item, items)
                            initRecyclerView(pro_Items.toProjecListItems())
                        }

                    }
//                    initRecyclerView(pro_Items)


                })
                createViewModel.costLineRate.observe(viewLifecycleOwner, Observer { cost ->
//                    totalCostTextView.text = cost.toString()
                    calculateTotalCost()
                })

            }


        })
        ItemTouchHelper(touchCallback).attachToRecyclerView(project_recyclerView)

    }




    private fun initRecyclerView(projecListItems: List<Project_Item>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(projecListItems)
//            for(item in projecListItems)
//            swipeSection += SwipeToDeleteItem(item, activity)

        }
                project_recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }


    }

    private fun List<ItemDTOTemp>.toProjecListItems(): List<Project_Item> {
        return this.map { approvej_items ->

            Project_Item(approvej_items, activity,createViewModel,contractID, job)

        }
    }





    private val touchCallback: SwipeTouchCallback by lazy {
        object : SwipeTouchCallback() {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = GroupAdapter<GroupieViewHolder>().getItem(viewHolder.adapterPosition)
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
//                    if (selectedProject == null) {
//                        toast("Error: selectedProject is null!")
//                    } else {

                    Navigation.findNavController(view)
                        .navigate(R.id.action_addProjectFragment_to_selectItemFragment)

//                        last_lin.visibility = View.GONE
//                        mid_lin.visibility = View.GONE
//                        selectProjectItemdrop.visibility = View.VISIBLE
                }
//                    last_lin.visibility = View.GONE
//                    mid_lin.visibility = View.GONE
//
//                    photoLin.visibility = View.GONE
//                    selectProjectItemdrop.visibility = View.VISIBLE

//                }

                R.id.resetButton -> {
                    onResetClicked(view)
                }

                R.id.infoTextView -> {
                    if (view.visibility == View.VISIBLE)
                        Navigation.findNavController(view).navigate(R.id.action_addProjectFragment_to_selectItemFragment)
//                    infoTextView.startAnimation(click)
                }

                R.id.startDateCardView -> {
                    startDateCardView.startAnimation(click)
                    // Get Current Date
                    val startDateCalender = Calendar.getInstance()
                    val startYear = startDateCalender[Calendar.YEAR]
                    val startMonth = startDateCalender[Calendar.MONTH]
                    val startDay = startDateCalender[Calendar.DAY_OF_MONTH]
                    startDateDialog = DatePickerDialog(
                        activity,
                        OnDateSetListener { view, year, month, dayOfMonth ->
                            setStartDateTextView(year, month, dayOfMonth)
                        }, startYear, startMonth, startDay
                    )
                    startDateDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
                    startDateDialog!!.show()
//                    startDateDialog?.getDatePicker()?.setMinDate(System.currentTimeMillis() - 1000)
//                    startDateDialog?.show()

                }

                R.id.dueDateCardView -> {
                    dueDateCardView.startAnimation(click) // Get Current Date
                    val c = Calendar.getInstance()
                    val year = c[Calendar.YEAR]
                    val month = c[Calendar.MONTH]
                    val day = c[Calendar.DAY_OF_MONTH]
                    dueDateDialog = DatePickerDialog(
                        activity,
                        OnDateSetListener { view, year, month, dayOfMonth ->
                            setDueDateTextView(year, month, dayOfMonth)
                        }, year, month, day
                    )
                    dueDateDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
                    dueDateDialog!!.show()

                }
                R.id.submitButton -> {

                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DATE, -1) // number represents number of days
                    val yesterday = cal.time.toString()


                    if (job != null) {
                        if (job?.StartDate == null) {
                            toast("Please select Start Date")
                            startDateCardView.startAnimation(shake_long)
                        }
                        if (job?.DueDate == null) {
                            toast("Please select Due Date")
                            dueDateCardView.startAnimation(shake_long)
                        }
                        if (job?.DueDate != null) {
                            if (job?.DueDate!! < job!!.StartDate!! || job!!.DueDate!! < yesterday || job?.DueDate!!.equals(yesterday)
                            ) {
//                                toast(R.string.end_date_error)
                            } else {
                                job?.DueDate
                            }
                        } else {
                            toast("Please select Due Date")
                            dueDateCardView.startAnimation(shake_long)
                        }
                        if (job!!.StartDate != null) {
                            if (job!!.StartDate!! < yesterday || job!!.DueDate!!.equals(
                                    yesterday
                                )
                            ) {
//                                toast(R.string.start_date_error)

                            } else {
                                job!!.StartDate
                            }
                        } else {
                            toast("Please select Start Date")
                            startDateCardView.startAnimation(shake_long)
                        }
                    }
                    Coroutines.main {
                    if (!JobUtils.areQuantitiesValid(job)) {
                        toast("Error: incomplete estimates.\n Quantity can't be zero!")
                        itemsCardView.startAnimation(shake_long)
                    } else  {
                        var valid  = createViewModel.areEstimatesValid(job, ArrayList<Any?>(items))
                               if (!valid){
                                  onInvalidJob()
                               }
                                else {
                                   if (job!!.DueDate == null) {
                                   toast("Select a due Date")
                                   dueDateCardView.startAnimation(shake_long)
                                   if (job!!.StartDate == null) {
                                       toast("Select a Start Date")
                                       startDateCardView.startAnimation(shake_long)
                                   }
                               } else {
                                   val prog = ProgressDialog(activity)
                                   prog.setTitle(getString(R.string.please_wait))
                                   prog.setMessage(getString(R.string.loading_job_wait))
                                   prog.setCancelable(false)
                                   prog.setIndeterminate(true)
                                   prog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                                   prog.show()

//                            val saved: Boolean = saveJob()
//                            if (job?.SectionId == null)
//                                verifyPhotoLocation() else if (isNetworkAvailable()) {
                                   job!!.IssueDate = Date().toString()
                                   submitJob(job!!,prog)

                                  }

//                                   toast("true")
                               }

                            }
                        }



                }

            }
        }

        addItemButton.setOnClickListener(myClickListener)
        resetButton.setOnClickListener(myClickListener)
        startDateCardView.setOnClickListener(myClickListener)
        dueDateCardView.setOnClickListener(myClickListener)
        submitButton.setOnClickListener(myClickListener)
        infoTextView.setOnClickListener(myClickListener)
    }

    private fun verifyPhotoLocation() {
//        offlineHelper.executeUpdatePhotosLocation(getJob(), object : OfflineCallBack() {
//            fun onCompleted(
//                message: String?,
//                countSuccess: Int,
//                countTotal: Int
//            ) {
//                if (countSuccess == countTotal && countTotal > 0) {
//                    toastShort("Route sections info updated!")
//                    submitJob()
//                } else {
//                    dismissProgressDialog()
//                    toast(message)
//                }
//            }
//        })
    }

    private fun submitJob(
        job: JobDTO,
        prog: ProgressDialog
    ) {
        val messages ="Please wait"

//
//        JobUtils.compressJobEstimates(tmpJob)
        if (job != null) {
           val jobTemp =  jobDataController.setJobLittleEndianGuids(job)!!
            saveRrmJob(job.UserId, jobTemp ,prog)

        }

    }

    private fun saveRrmJob(
        userId: Int,
        job: JobDTO,
        prog: ProgressDialog
    ){
     Coroutines.main {
        val submit =
            createViewModel.submitJob(userId, job, activity!!)

         if (submit != null){
             prog.dismiss()
             toast(submit) }else {
             prog.dismiss()
             toast(R.string.job_submitted)
             popViewOnJobSubmit()}

     }

    }

    private fun popViewOnJobSubmit() {
        // TODO("delete Items data from database after success upload")
        Intent(context?.applicationContext  , MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }


    private fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        dueDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        dueDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job?.DueDate = calendar.time.toString()
    }

    private fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        startDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        startDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        job?.StartDate = calendar.time.toString()
    }

    private fun onResetClicked(view: View) {
        Coroutines.main {
            createViewModel.deleteItemList(job!!.JobId)
            createViewModel.deleJobfromList(job!!.JobId)
//                        createViewModel.deleJobItemList(newjob!!.JobId)
        }
        resetContractAndProjectSelection(view)
    }

    private fun calculateTotalCost() {
        totalCostTextView.text = JobUtils.formatTotalCost(job)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putSerializable("selectedContract", selectedContract)
//        outState.putSerializable("selectedProject", selectedProject)
//        outState.putSerializable("job", job)
        outState.putSerializable("items", items as ArrayList<ProjectItemDTO>)
//        outState.putBoolean("isJobSaved", isJobSaved)
//        outState.putSerializable("estimatesToRemoveFromDb", estimatesToRemoveFromDb)
        super.onSaveInstanceState(outState)
    }


//     fun onRestoreInstanceState(inState: Bundle) {
//        items = inState.getSerializable("items") as ArrayList<ProjectItemDTO>
//
//    }

    private fun resetContractAndProjectSelection(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_addProjectFragment_to_nav_create)
        infoTextView.text = null
//        adapter?.setData(items)
//        descriptionEditText.text!!.clear()
////        mid_lin.visibility = View.GONE
////        last_lin.visibility = View.GONE
////        selectProjectItemdrop.visibility = View.GONE
////        photoLin.visibility = View.GONE
//        selectProjectLayout.visibility = View.VISIBLE
//        setLayoutsVisibility()
//        setMenuItems()
    }



     fun onInvalidJob() {
        toast("Incomplete estimates!")
        itemsCardView.startAnimation(shake_long)
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onStop() {
        super.onStop()
        Log.d("Tag", "FragmentA.onDestroyView() has been called.");
    }



    //    private fun initRecyclerView(
//        item: ItemDTOTemp,
//        items: MutableList<ItemDTOTemp>
//    ) {
//        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
//            //            toast(itemList.size.toString())
//
////            for (item in itemList.listIterator()) {
////                items.add(item)
////                swipeSection += SwipeToDeleteItem(item, activity)
//            swipeSection += SwipeToDeleteItem(item, activity)
//                add(swipeSection)
//
////            }
//
//        }
//
//
//        project_recyclerView.apply {
//            layoutManager = LinearLayoutManager(this.context)
//            adapter = groupAdapter
//        }
//        groupAdapter.setOnItemClickListener { item, view ->
//
//            (item as? Project_Item)?.let {
//
////                toast(it.getPosition(it).toString())
//                //                sendSelectedItem((it.itemDTO.itemCode +"  "+ it.itemDTO.descr),(it.itemDTO.tenderRate) , view)
////                sendSelectedItem((it),proItem,job,useR, view)
////                sendSelectedItem((it), view)
//            }
//
//
//        }
//    }


}




