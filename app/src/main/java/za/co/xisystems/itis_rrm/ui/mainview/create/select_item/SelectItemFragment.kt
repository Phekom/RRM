package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_select_item.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.util.*
import java.util.concurrent.CancellationException

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class SelectItemFragment : BaseFragment(R.layout.fragment_select_item), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var items: MutableList<ItemDTOTemp> = ArrayList<ItemDTOTemp>()
    private var animate = false

    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var  itemSections : ArrayList<ItemSectionDTO>
    @MyState
    internal var selectedSectionItem: SectionItemDTO? = null

    @MyState
    internal var useR: Int? = null

    @MyState
    var newJob: JobDTO? = null

    @MyState
    lateinit var editJob: JobDTO
    private var uiScope = UiLifecycleScope()

    init {

        lifecycleScope.launch {
            whenCreated {
                uiScope.onCreate()
            }
            whenStarted {
                viewLifecycleOwner.lifecycle.addObserver(uiScope)

                uiScope.launch(context = uiScope.coroutineContext, start = CoroutineStart.DEFAULT) {
                    createViewModel.loggedUser.observe(viewLifecycleOwner, Observer { user ->
                        useR = user
                    })

                    createViewModel.projectId.observe(viewLifecycleOwner, Observer { projectId ->
                        setItemsBySections(projectId)
                    })

                    createViewModel.newJob.observe(viewLifecycleOwner, Observer { newJ ->
                        newJob = newJ
                    })

                    createViewModel.jobToEditItem.observe(
                        viewLifecycleOwner,
                        Observer { newJ_Edit ->
                            setItemsBySections(newJ_Edit.ProjectId!!)
                            editJob = newJ_Edit
                        })
                }
            }
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.select_item_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_select_item, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)

        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        uiScope.run {

            itemSections = ArrayList<ItemSectionDTO>()
            newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
            bindUI()
        }

    }

    private fun bindUI() {
        // moved to init
    }

    private fun setItemsBySections(projectId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val dialog =
                setDataProgressDialog(
                    requireActivity(),
                    getString(R.string.data_loading_please_wait)
                )

            // SectionItems filtered by projectId
            val sectionItems = createViewModel.getSectionItemsForProject(projectId)
            dialog.show()

            sectionItems.observe(viewLifecycleOwner, Observer { sectionData ->
                val sectionSelections = arrayOfNulls<String?>(sectionData.size)
                dialog.dismiss()
                for (item in sectionData.indices) {
                    sectionSelections[item] = sectionData[item].description
                }
                uiScope.launch(uiScope.coroutineContext) {
                    setSpinner(
                        requireContext().applicationContext,
                        sectionItemSpinner,
                        sectionData,
                        sectionSelections,
                        object : SpinnerHelper.SelectionListener<SectionItemDTO> {

                            override fun onItemSelected(position: Int, item: SectionItemDTO) {
                                if (animate) {
                                    sectionItemSpinner.startAnimation(bounce_750)
                                    item_recyclerView.startAnimation(bounce_1000)
                                }
                                selectedSectionItem = item
                                setRecyclerItems(projectId, item.sectionItemId!!)
                            }

                        })

                    sectionItemSpinner.setOnTouchListener { v, event ->
                        animate = true
                        false
                    }

                }

            })

        }
    }


    /**
     * Get only the section items relevant for the section, filtered by what is relevant
     * to the Project
     * @param projectId String
     * @param sectionItemId String
     */
    private fun setRecyclerItems(projectId: String, sectionItemId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val projectsItems =
                createViewModel.getAllItemsForSectionItemByProjectId(sectionItemId, projectId)
            projectsItems.observe(viewLifecycleOwner, Observer { projectItemList ->
                group_loading.visibility = View.GONE
                initRecyclerView(projectItemList.toProjectItems())
            })
        }

    }

    private fun List<ProjectItemDTO>.toProjectItems(): List<SectionProj_Item> {
        return this.map { projectItem ->
            SectionProj_Item(projectItem)
        }
    }

    private fun initRecyclerView(items: List<SectionProj_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {

            addAll(items)
        }

        item_recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        groupAdapter.setOnItemClickListener { item, view ->

            (item as? SectionProj_Item)?.let {

                val tempItem = createItemList(it.itemDTO, itemSections)
                saveNewItem(tempItem)
                sendSelectedItem((it) , view,items )
            }


        }


    }

    private fun saveNewItem(tempItem: ItemDTOTemp) {
        uiScope.launch(context = uiScope.coroutineContext) {
            createViewModel.saveNewItem(tempItem)
        }
    }

    private fun createItemList(
        itemDTO: ProjectItemDTO,
        itemSections: ArrayList<ItemSectionDTO>
    ): ItemDTOTemp {
        val newItem = ItemDTOTemp(
            id = 0,
            itemId = itemDTO.itemId,
            descr = itemDTO.descr,
            itemCode = itemDTO.itemCode,
            itemSections = itemSections,
            tenderRate = itemDTO.tenderRate,
            uom = itemDTO.uom,
            workflowId = itemDTO.workflowId,
            sectionItemId = itemDTO.sectionItemId,
            quantity = itemDTO.quantity,
            estimateId = itemDTO.estimateId,
            projectId = itemDTO.projectId!!,
            jobId = newJob?.JobId ?: editJob.JobId
        )
        items.add(newItem)
        return newItem
    }

    private fun sendSelectedItem(
        item: SectionProj_Item,
        view: View,
        jobArrayList: List<SectionProj_Item>
    ) {
        val myList = jobArrayList

        Coroutines.main {
            createViewModel.setSectionProjectItem(item)
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_selectItemFragment_to_addProjectFragment)
    }

    override fun onDestroyView() {
        // Prevents RecyclerView Memory leak
        item_recyclerView.adapter = null
        uiScope.job.cancel(CancellationException("onDestroyView"))
        viewLifecycleOwner.lifecycleScope.cancel(CancellationException("onDestroyView"))
        super.onDestroyView()
    }

}