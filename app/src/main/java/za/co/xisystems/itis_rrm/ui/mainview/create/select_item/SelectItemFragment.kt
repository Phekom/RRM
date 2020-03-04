package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_select_item.*
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
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.util.*


/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class SelectItemFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private lateinit var iTemArrayList: ArrayList<JobDTO>
    private var items: MutableList<ItemDTOTemp> = ArrayList<ItemDTOTemp>()
    private var animate = false
    private val itemsMap: MutableMap<String, MutableList<ProjectItemDTO>?> =
        LinkedHashMap<String, MutableList<ProjectItemDTO>?>()

    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var  itemSections : ArrayList<ItemSectionDTO>
    @MyState
    internal var selectedSectionitem: SectionItemDTO? = null
    @MyState
    internal var selectedProjectitem: ProjectItemDTO? = null
    internal var useR: Int? = null

    @MyState
    lateinit var job: JobDTO

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
        Coroutines.main {
            itemSections = ArrayList()
            newJobItemEstimatesList = ArrayList()
            bindUI()
        }

    }

    private fun bindUI() {

        createViewModel.loggedUser.observe(viewLifecycleOwner, Observer { user ->
            useR = user
//            selectedContractTextView.text = user
        })
        createViewModel.proId.observe(viewLifecycleOwner, Observer { pro_Id ->
            setItemsBySections(pro_Id)
//            setItems(pro_Id)
        })

        createViewModel.newjob.observe(viewLifecycleOwner, Observer { newJ ->
                     job = newJ
        })


        createViewModel.jobtoEdit_Item.observe(viewLifecycleOwner, Observer { newJ_Edit ->
            setItemsBySections(newJ_Edit.ProjectId!!)
            job = newJ_Edit
        })


    }

    private fun setItemsBySections(projectId: String) {
        Coroutines.main {
            val sectionItems = createViewModel.getAllSectionItem()
//            val sectionItems = createViewModel.offlinedata.await()
            sectionItems.observe(viewLifecycleOwner, Observer { sec_tions ->
                val sections = sec_tions
                val sectionNmbr = arrayOfNulls<String?>(sections.size)

                for (item in sections.indices) {
                    sectionNmbr[item] = sections[item].description
                }
                Coroutines.main {
                    setSpinner(context!!.applicationContext,
                        sectionItemSpinner,
                        sections,
                        sectionNmbr,
                        object : SpinnerHelper.SelectionListener<SectionItemDTO> {

                            override fun onItemSelected(position: Int, item: SectionItemDTO) {
                                if (animate) {
                                    sectionItemSpinner.startAnimation(bounce_750)
                                    item_recyclerView.startAnimation(bounce_1000)
                                }
                                selectedSectionitem = item
                                setRecyclerItems(projectId, item.sectionItemId!!)
                                //                                setRecyclerItems(item.sectionItemId)
                            }

                        })

                    sectionItemSpinner.setOnTouchListener { v, event ->
                        animate = true
                        false
                    }

                }

            })

//            bindUI()
        }
    }


    //    private fun setRecyclerItems(sectionItemId: String) {
    private fun setRecyclerItems(projectId: String, sectionItemId: String) {
        Coroutines.main {  //data_loading2.show()
            //            val projectsItems = createViewModel.getItemForItemCode(sectionItemId!!)
            val projectsItems = createViewModel.getAllItemsForSectionItem(sectionItemId, projectId)
            projectsItems.observe(viewLifecycleOwner, Observer { i_tems ->
                group_loading.visibility = View.GONE
                initRecyclerView(i_tems.toProjectItems())
                //                data_loading2.hide()
                //                group_loading.visibility = View.GONE
            })

        }

    }

    private fun List<ProjectItemDTO>.toProjectItems(): List<SectionProj_Item> {
        return this.map { proj_items ->
            SectionProj_Item(proj_items)
        }
    }

    private fun initRecyclerView(items: List<SectionProj_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {

            addAll(items)
        }
        item_recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }

        groupAdapter.setOnItemClickListener { item, view ->

            (item as? SectionProj_Item)?.let {
//                sendSelectedItem((it.itemDTO.itemCode +"  "+ it.itemDTO.descr),(it.itemDTO.tenderRate) , view)
                val newjItem = createItemList(it.itemDTO, itemSections)
                saveNewItem(newjItem)
                sendSelectedItem((it) , view,items )
//                if (job.JobId == null) { // New Job
//                }
            }


        }


    }

    private fun saveNewItem(newjItem: ItemDTOTemp) {
        Coroutines.main {
            createViewModel.saveNewItem(newjItem)
        }
    }

    private fun createItemList(
        itemDTO: ProjectItemDTO,
        itemSections: ArrayList<ItemSectionDTO>
    ): ItemDTOTemp {
        val newItem = ItemDTOTemp(
            0,itemDTO.itemId,itemDTO.descr,itemDTO.itemCode,itemSections,itemDTO.tenderRate,itemDTO.uom,itemDTO.workflowId,
            itemDTO.sectionItemId,itemDTO.quantity,itemDTO.estimateId, itemDTO.projectId!!, job.JobId
        )
        items.add(newItem)
        return newItem
    }





    private fun sendSelectedItem(
        item: SectionProj_Item,
        view: View,
        jobArrayList: List<SectionProj_Item>
    ) {
        val selecteD = item
        val myList = jobArrayList
//        val actionAddProject =  SelectItemFragmentDirection.actionAddProject(selecteD)
//        navController?.navigate(R.id.action_selectItemFragment_to_addProjectFragment)


        Coroutines.main {
            createViewModel.Sec_Item.value = selecteD
//            createViewModel.project_Rate.value = selectRte
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_selectItemFragment_to_addProjectFragment)
    }

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


//    private fun setItems(sectionItemId: String,projectId: String) {
//        Coroutines.main {
////            data_loading2.show()
////            try {
//            val projectsItems = createViewModel.getAllItemsForSectionItem(sectionItemId ,projectId)
////            val projectsItems = createViewModel.getAllItemsForProjectId(projectId)
////                val projectsItems = authViewModel.projectsItems.await()
//            projectsItems?.observe(viewLifecycleOwner, Observer { i_tems ->
//
//                val items = i_tems
//                val itemNmbr = arrayOfNulls<String>(i_tems.size)
//
////                for (project in i_tems.indices) {
//////                    filterItem(i_tems[project].sectionItemId)
////                    itemNmbr[project] = i_tems[project].itemCode + "  " +  //?.dropLast(2)
////                            i_tems[project].descr //+ "  " +  "( " + i_tems[project].uom + " )"
////                }
////                Coroutines.main {
////                    setSpinner(context!!.applicationContext, sectionItemSpinner, items, itemNmbr,
////                        object : SpinnerHelper.SelectionListener<ItemDTO> {
////
////                            override fun onItemSelected(position: Int, item: ItemDTO) {
////                                if (animate) {
////                                    sectionItemSpinner.startAnimation(bounce_750)
////                                    itemsDropCardView.startAnimation(bounce_1000)
////                                }
////                                selectedProjectitem = item
////
////                                if (position == 0) {
////                                    filterItem(null)
////                                } else {
////                                    filterItem(item.sectionItemId)
////                                    setRecyclerItems(item.sectionItemId)
////                                }
////
////                            }
////
////                        })
////
////                    sectionItemSpinner.setOnTouchListener { v, event ->
////                        animate = true
////                        false
////                    }
////
////            }
//
//            })
//
//
//
//
////            } catch (e: ApiException) {
////                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
////            } catch (e: NoInternetException) {
////                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
////            }
//        }
//    }
