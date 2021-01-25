/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

/*******************************************************************************
 * Updated by Shaun McDonald on 2021/29/25
 * Last modified on 2021/01/25 3:23 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 ******************************************************************************/

package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import java.util.ArrayList
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.databinding.FragmentSelectItemBinding
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class SelectItemFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var items: MutableList<ItemDTOTemp> = ArrayList()
    private var animate = false

    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    // viewBinding implementation
    private var _ui: FragmentSelectItemBinding? = null
    private val ui get() = _ui!!

    @MyState
    internal var selectedSectionItem: SectionItemDTO? = null

    @MyState
    internal var useR: Int? = null

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
                    createViewModel.loggedUser.observe(viewLifecycleOwner, { user ->
                        useR = user
                    })

                    createViewModel.projectId.observe(viewLifecycleOwner, { projectId ->
                        setItemsBySections(projectId)
                    })

                    createViewModel.currentJob.observe(viewLifecycleOwner, { newJ ->
                        newJ?.let {
                            editJob = it
                            setItemsBySections(it.projectId!!)
                        }
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
        // no options menu
        return false // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _ui = FragmentSelectItemBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        itemSections = ArrayList()
        newJobItemEstimatesList = ArrayList()
    }

    private fun setItemsBySections(projectId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {

            // SectionItems filtered by projectId
            val sectionItems = createViewModel.getSectionItemsForProject(projectId)
            ui.dataLoading2.visibility = View.VISIBLE

            sectionItems.observe(viewLifecycleOwner, { sectionData ->
                val sectionSelections = arrayOfNulls<String?>(sectionData.size)
                ui.dataLoading2.visibility = View.GONE
                for (item in sectionData.indices) {
                    sectionSelections[item] = sectionData[item].description
                }
                uiScope.launch(uiScope.coroutineContext) {
                    setSpinner(
                        requireContext().applicationContext,
                        ui.sectionItemSpinner,
                        sectionData,
                        sectionSelections,
                        object : SpinnerHelper.SelectionListener<SectionItemDTO> {

                            override fun onItemSelected(position: Int, item: SectionItemDTO) {
                                if (animate) {
                                    ui.sectionItemSpinner.startAnimation(bounce_750)
                                    ui.itemRecyclerView.startAnimation(bounce_1000)
                                }
                                selectedSectionItem = item
                                setRecyclerItems(projectId, item.sectionItemId)
                            }
                        })

                    ui.sectionItemSpinner.setOnTouchListener { _, _ ->
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
            projectsItems.observe(viewLifecycleOwner, { projectItemList ->
                ui.groupLoading.visibility = View.GONE
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
            notifyDataSetChanged()
        }

        ui.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        groupAdapter.setOnItemClickListener { item, view ->

            (item as? SectionProj_Item)?.let {

                val tempItem = createItemList(it.itemDTO, itemSections)
                saveNewItem(tempItem)
                sendSelectedItem((it), view)
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
            jobId = editJob.jobId
        )
        items.add(newItem)
        return newItem
    }

    private fun sendSelectedItem(
        item: SectionProj_Item,
        view: View
    ) {

        Coroutines.main {
            createViewModel.setSectionProjectItem(item)
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_selectItemFragment_to_addProjectFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevents RecyclerView Memory leak
        ui.itemRecyclerView.adapter = null
        uiScope.destroy()
        viewLifecycleOwner.lifecycleScope.cancel(CancellationException("onDestroyView"))
        _ui = null
    }
}
