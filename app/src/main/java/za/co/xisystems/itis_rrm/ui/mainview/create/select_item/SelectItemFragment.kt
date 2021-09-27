/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:57
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

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
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
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
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.util.ArrayList
import java.util.concurrent.CancellationException

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class SelectItemFragment : BaseFragment(), DIAware {
    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var items: MutableList<ItemDTOTemp> = ArrayList()
    private var animate = false

    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    // viewBinding implementation
    private var _ui: FragmentSelectItemBinding? = null
    private val ui get() = _ui!!

    internal var selectedSectionItem: SectionItemDTO? = null

    private var useR: Int? = null

    private lateinit var editJob: JobDTO
    private var uiScope = UiLifecycleScope()

    init {

        lifecycleScope.launch {
            whenCreated {
                uiScope.onCreate()
            }
            whenStarted {
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
                initUI()
            }
            whenResumed {
                if (useR == null) {
                    initUI()
                }
            }
        }
    }

    private fun initUI() {
        uiScope.launch(context = uiScope.coroutineContext, start = DEFAULT) {
            createViewModel.loggedUser.observe(viewLifecycleOwner, { user ->
                useR = user
            })

            createViewModel.itemJob.observe(viewLifecycleOwner, { newJ ->
                newJ.getContentIfNotHandled()?.let {
                    editJob = it
                    setItemsBySections(it.projectId!!)
                }
            })
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
        var stateRestored = false
        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        itemSections = ArrayList()
        newJobItemEstimatesList = ArrayList()

        val args by navArgs<SelectItemFragmentArgs>()

        if (!args.jobId.isNullOrBlank()) {
            onRestoreSavedState(args.toBundle())
            stateRestored = true
        }

        if (savedInstanceState != null && !stateRestored) {
            onRestoreSavedState(savedInstanceState)
        }
    }

    /**
     * Called when all saved state has been restored into the view hierarchy
     * of the fragment.  This can be used to do initialization based on saved
     * state that you are letting the view hierarchy track itself, such as
     * whether check box widgets are currently checked.  This is called
     * after [.onActivityCreated] and before
     * [.onStart].
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    private fun onRestoreSavedState(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let {
            it.run {
                val jobId = getString("jobId", "")
                if (jobId.isNotBlank()) {
                    createViewModel.setItemJob(jobId)
                    initUI()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString("jobId", editJob.jobId)
        }
        super.onSaveInstanceState(outState)
    }

    private fun setItemsBySections(projectId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val sectionItems = createViewModel.getSectionItemsForProject(projectId)
            ui.dataLoading2.visibility = View.VISIBLE

            sectionItems.observe(viewLifecycleOwner, { sectionData ->
                val sectionSelections = arrayOfNulls<String?>(sectionData.size)
                ui.dataLoading2.visibility = View.GONE
                for (item in sectionData.indices) {
                    sectionSelections[item] = sectionData[item].description
                }

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

    private fun List<ProjectItemDTO>.toProjectItems(): List<SectionProjectItem> {
        return this.map { projectItem ->
            SectionProjectItem(projectItem)
        }
    }

    private fun initRecyclerView(items: List<SectionProjectItem>) {
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

            (item as? SectionProjectItem)?.let {

                val tempItem = createItemList(it.itemDTO, itemSections)
                saveNewItem(tempItem)
                sendSelectedItem(tempItem, view)
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
        item: ItemDTOTemp,
        view: View
    ) {

        Coroutines.main {
            createViewModel.setTempProjectItem(item)
        }

        val directions = SelectItemFragmentDirections.actionSelectItemFragmentToAddProjectFragment2(
            editJob.projectId,
            editJob.jobId
        )

        Navigation.findNavController(view).navigate(directions)
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
