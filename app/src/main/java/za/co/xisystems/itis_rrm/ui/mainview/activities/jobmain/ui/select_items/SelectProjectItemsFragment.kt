package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.select_items

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.databinding.FragmentSelectItemsBinding
import za.co.xisystems.itis_rrm.databinding.ProjectItemBinding
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.SpinnerHelper
import java.util.*
import java.util.concurrent.CancellationException

class SelectProjectItemsFragment : BaseFragment() {

    private lateinit var selectItemsViewModel: SelectItemsViewModel
    private var _binding: FragmentSelectItemsBinding? = null
    private val factory: SelectItemsViewModelFactory by instance()
    private val binding get() = _binding!!

    private var animate = false
    private val projectItemArgsData: SelectProjectItemsFragmentArgs by navArgs()
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>
    internal var selectedSectionItem: SectionItemDTO? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var jOB_ACTIVITY: JobCreationActivity = context as JobCreationActivity
        jOB_ACTIVITY.navigationView.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        selectItemsViewModel =
            ViewModelProvider(this, factory).get(SelectItemsViewModel::class.java)
        _binding = FragmentSelectItemsBinding.inflate(inflater, container, false)
        _binding?.toolbar?.apply {
            backImage.visibility = View.GONE
            setTitle(getString(R.string.select_item_title))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemSections = ArrayList()
        newJobItemEstimatesList = ArrayList()
        if (projectItemArgsData.contractVoId.isNullOrEmpty()){
            setItemsBySections(projectItemArgsData.projectId!!)
        } else{
            setItemsBySections2(projectItemArgsData.contractVoId!!)
        }


    }

    private fun setItemsBySections2(contractVoId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val sectionItems = selectItemsViewModel.getSectionItemsForProject2(contractVoId)
            binding.dataLoading2.visibility = View.VISIBLE

            sectionItems.observe(viewLifecycleOwner, { sectionData ->
                val sectionSelections = arrayOfNulls<String?>(sectionData.size)
                binding.dataLoading2.visibility = View.GONE
                for (item in sectionData.indices) {
                    sectionSelections[item] = sectionData[item].description
                }

                SpinnerHelper.setSpinner(
                    requireContext().applicationContext,
                    binding.sectionItemSpinner,
                    sectionData,
                    sectionSelections,
                    object : SpinnerHelper.SelectionListener<SectionItemDTO> {

                        override fun onItemSelected(position: Int, item: SectionItemDTO) {
                            if (animate) {
                                binding.sectionItemSpinner.startAnimation(bounce_750)
                                binding.itemRecyclerView.startAnimation(bounce_1000)
                            }
                            selectedSectionItem = item
                            setRecyclerItems2(contractVoId, item.sectionItemId, )
                        }
                    }
                )
                binding.sectionItemSpinner.setOnTouchListener { view, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_UP -> {
                            view.performClick()
                        }
                        else -> {
                            // MotionEvent.ACTION_DOWN happened
                        }
                    }
                    animate = true
                    false
                }
            })
        }
    }

    private fun setItemsBySections(projectId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val sectionItems = selectItemsViewModel.getSectionItemsForProject(projectId)
            binding.dataLoading2.visibility = View.VISIBLE

            sectionItems.observe(viewLifecycleOwner, { sectionData ->
                val sectionSelections = arrayOfNulls<String?>(sectionData.size)
                binding.dataLoading2.visibility = View.GONE
                for (item in sectionData.indices) {
                    sectionSelections[item] = sectionData[item].description
                }

                SpinnerHelper.setSpinner(
                    requireContext().applicationContext,
                    binding.sectionItemSpinner,
                    sectionData,
                    sectionSelections,
                    object : SpinnerHelper.SelectionListener<SectionItemDTO> {

                        override fun onItemSelected(position: Int, item: SectionItemDTO) {
                            if (animate) {
                                binding.sectionItemSpinner.startAnimation(bounce_750)
                                binding.itemRecyclerView.startAnimation(bounce_1000)
                            }
                            selectedSectionItem = item
                            setRecyclerItems(projectId, item.sectionItemId)
                        }
                    }
                )
                binding.sectionItemSpinner.setOnTouchListener { view, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_UP -> {
                            view.performClick()
                        }
                        else -> {
                            // MotionEvent.ACTION_DOWN happened
                        }
                    }
                    animate = true
                    false
                }
            })
        }
    }

    private fun setRecyclerItems(projectId: String, sectionItemId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val projectsItems =
                selectItemsViewModel.getAllItemsForSectionItemByProjectId(sectionItemId, projectId)
            projectsItems.observe(viewLifecycleOwner, { projectItemList ->
                binding.groupLoading.visibility = View.GONE
                initRecyclerView(projectItemList.toProjectItems(projectId))
            })
        }
    }

    private fun setRecyclerItems2(contractVoId: String, sectionItemId: String) {
        uiScope.launch(context = uiScope.coroutineContext) {
            val projectsItems =
                selectItemsViewModel.getAllItemsForSectionItemByContractVoId(sectionItemId, contractVoId)
            projectsItems.observe(viewLifecycleOwner, { projectItemList ->
                binding.groupLoading.visibility = View.GONE
                initRecyclerView2(projectItemList.toContractVoItems(contractVoId))
            })
        }
    }

    private fun List<VoItemDTO>.toContractVoItems(contractVoId: String): List<SectionVoItem> {
        return this.map { projectItem ->
            SectionVoItem(projectItem, itemSections, projectItemArgsData,contractVoId, selectItemsViewModel)
        }
    }

    private fun List<ProjectItemDTO>.toProjectItems(projectId: String): List<SectionProjectItem> {
        return this.map { projectItem ->
            SectionProjectItem(projectItem, itemSections, projectItemArgsData,projectId, selectItemsViewModel)
        }
    }

    private fun initRecyclerView(items: List<SectionProjectItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder<ProjectItemBinding>>().apply {
            addAll(items)
            notifyItemRangeChanged(0, items.size)
        }

        binding.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

    }

    private fun initRecyclerView2(items: List<SectionVoItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder<ProjectItemBinding>>().apply {
            addAll(items)
            notifyItemRangeChanged(0, items.size)
        }

        binding.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevents RecyclerView Memory leak
        binding.itemRecyclerView.adapter = null
        viewLifecycleOwner.lifecycleScope.cancel(CancellationException("onDestroyView"))
        _binding = null
    }
}