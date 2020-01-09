package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_add_project_items.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.*


class AddProjectFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var dueDateDialog: DatePickerDialog? = null
    private var startDateDialog: DatePickerDialog? = null
    @MyState
    internal var job: JobDTO? = null
    @MyState
    var items: ArrayList<ItemDTO> = ArrayList<ItemDTO>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        calculateTotalCost()
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
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java) } ?: throw Exception("Invalid Activity")



        last_lin.visibility = View.GONE

        createViewModel.contract_No.observe(viewLifecycleOwner, Observer { contrct ->
            selectedContractTextView.text = contrct
        })
        createViewModel.project_Code.observe(viewLifecycleOwner, Observer { pro_code ->
            selectedProjectTextView.text = pro_code
        })


        //        mid_lin.visibility = View.GONE

//        selectProjectItemdrop.visibility = View.GONE
//        photoLin.visibility = View.GONE


        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.addItemButton -> {
//                    if (selectedProject == null) {
//                        toast("Error: selectedProject is null!")
//                    } else {
                    Navigation.findNavController(view).navigate(R.id.action_addProjectFragment_to_selectItemFragment)

//                        last_lin.visibility = View.GONE
//                        mid_lin.visibility = View.GONE
//                        selectProjectItemdrop.visibility = View.VISIBLE
//                    }
//                    last_lin.visibility = View.GONE
//                    mid_lin.visibility = View.GONE
//
//                    photoLin.visibility = View.GONE
//                    selectProjectItemdrop.visibility = View.VISIBLE

                }

                R.id.resetButton -> {
                    onResetClicked(view)
                }

                R.id.infoTextView -> {
                    infoTextView.startAnimation(click)
                }

                R.id.startDateCardView -> {
                    startDateCardView.startAnimation(click)
                    // Get Current Date
                    val startDateCalender = Calendar.getInstance()
                    val startYear = startDateCalender[Calendar.YEAR]
                    val startMonth = startDateCalender[Calendar.MONTH]
                    val startDay = startDateCalender[Calendar.DAY_OF_MONTH]
                    startDateDialog = DatePickerDialog(activity,
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
                    dueDateCardView.startAnimation(click)
                    // Get Current Date
                    val c = Calendar.getInstance()
                    val year = c[Calendar.YEAR]
                    val month = c[Calendar.MONTH]
                    val day = c[Calendar.DAY_OF_MONTH]
                    dueDateDialog = DatePickerDialog(activity,
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
                    val yesterday = cal.time

                    if (getJob() != null) {
                        if (getJob()?.StartDate == null) {
                            toast("Please select Start Date")
                            startDateCardView.startAnimation(shake_long)
                        }
                        if (getJob()?.DueDate == null) {
                            toast("Please select Due Date")
                            dueDateCardView.startAnimation(shake_long)
                        }
                        if (getJob()?.DueDate != null) {
                            if (getJob()?.DueDate!! < getJob()!!.StartDate!! || getJob()!!.DueDate!! < yesterday.toString() || getJob()?.DueDate!!.equals(yesterday)
                            ) {
                                toast(R.string.end_date_error)
                            } else {
                                getJob()?.DueDate
                            }
                        } else {
                            toast("Please select Due Date")
                            dueDateCardView.startAnimation(shake_long)
                        }
                        if (getJob()!!.StartDate != null) {
                            if (getJob()!!.StartDate!! < yesterday.toString() || getJob()!!.DueDate!!.equals(yesterday)
                            ) {
                                toast(R.string.start_date_error)

                            } else {
                                 getJob()!!.StartDate
                            }
                        } else {
                            toast("Please select Start Date")
                            startDateCardView.startAnimation(shake_long)
                        }
                    }

                    if (!JobUtils.areQuantitiesValid(job)) {
                        toast("Error: incomplete estimates.\n Quantity can't be zero!")
                        itemsCardView.startAnimation(shake_long)
                    } else
//                        if (jobSubmitViewModel.areEstimatesValid(
//                            getJob(),
//                            ArrayList<Any>(items)
//                        )
//                    )
                        {
                        if (getJob()!!.DueDate == null) {
                            toast("Select a due Date")
                            dueDateCardView.startAnimation(shake_long)
                            if (getJob()!!.StartDate == null) {
                                toast("Select a Start Date")
                                startDateCardView.startAnimation(shake_long)
                            }
                        } else {
//                            val saved: Boolean = saveJob()
//                            if (getJob().getSectionId() == null) verifyPhotoLocation() else if (isNetworkAvailable()) {
//                                submitJob()
//                            } else if (saved) {
//                                saveJob()
//                            } else toast("Error saving job")
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


        createViewModel.project_Item.observe(viewLifecycleOwner, Observer { pro_Item ->
            last_lin.visibility = View.VISIBLE
            setRecyclerItems(pro_Item)
//            setItemsBySections(pro_Id)
//            setItems(pro_Id)


//            infoTextView.text = pro_Item
        })


    }

    private fun setRecyclerItems(proItem: String?) {
//        initRecyclerView(i_tems.toProjectItems())
    }
    private fun initRecyclerView(item: Project_Item) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            add(item)
        }
        project_recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }

        groupAdapter.setOnItemClickListener { item, view ->

            (item as? Project_Item)?.let {
                sendSelectedItem((it.itemDTO.itemCode +"  "+ it.itemDTO.descr), view)
            }


//            navController?.navigate(R.id.action_selectItemFragment_to_addProjectFragment)
//            Coroutines.main {
//               createViewModel.project_Item.value = selectedSectionitem?.itemCode.toString() + selectedSectionitem?.description.toString()
//
//            }

//            val intent = Intent()
//            intent.putExtra("item", item)
//            setResult(RESULT_OK, intent)
//            finish()

        }


    }
    private fun List<ItemDTO>.toProjectItems(): List<Project_Item> {
        return this.map { proj_items ->
            Project_Item(proj_items)
        }
    }
    private fun sendSelectedItem(item: String?, view: View) {
        val selecteD = item.toString()
//        val actionAddProject =  SelectItemFragmentDirection.actionAddProject(selecteD)
//        navController?.navigate(R.id.action_selectItemFragment_to_addProjectFragment)


//        Coroutines.main {
//            createViewModel.project_Item.value = selecteD
//        }
//
//        Navigation.findNavController(view)
//            .navigate(R.id.action_selectItemFragment_to_addProjectFragment)
    }

    private fun setDueDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        dueDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        dueDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        getJob()?.DueDate = calendar.time.toString()
    }

    private fun setStartDateTextView(year: Int, month: Int, dayOfMonth: Int) {
        startDateTextView.text = DateUtil.toStringReadable(year, month, dayOfMonth)
        startDateCardView.startAnimation(bounce_500)
        val calendar = Calendar.getInstance()
        calendar[year, month] = dayOfMonth
        getJob()?.StartDate = calendar.time.toString()
    }

    private fun onResetClicked(view :View) {
//        setJob(null)
        resetContractAndProjectSelection(view)
    }
    private fun calculateTotalCost() {
        totalCostTextView.setText(JobUtils.formatTotalCost(getJob()!!))
    }
    fun getJob(): JobDTO? {
        return job
    }
    private fun resetContractAndProjectSelection(view : View) {
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











}