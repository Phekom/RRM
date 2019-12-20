package za.co.xisystems.itis_rrm.ui.mainview.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_select_item.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.SpinnerHelper.setSpinner
import za.co.xisystems.itis_rrm.utils.*


class SelectItemFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    @MyState
    internal var selectedProjectitem: ItemDTO? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.select_item_title)
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
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        Coroutines.main {
           bindUI()
        }

    }

    private fun bindUI() {
        createViewModel.proId.observe(viewLifecycleOwner, Observer { pro_Id ->
            setItems(pro_Id)
        })
    }

    private fun setItems(projectId: String) {
        Coroutines.main {
            data_loading2.show()
            try {
                val projectsItems = createViewModel.getAllItemsForProjectId(projectId)
//                val projectsItems = authViewModel.projectsItems.await()
                projectsItems?.observe(viewLifecycleOwner, Observer { i_tems ->
                    data_loading2.hide()
                    val items = i_tems
                    val itemNmbr = arrayOfNulls<String>(i_tems.size)
                    for (project in i_tems.indices) {
                        itemNmbr[project] = i_tems[project].itemCode +"  "+
                                      i_tems[project].descr +"  "+
                                    "( " +  i_tems[project].uom + " )"

                    }
                    setSpinner(context!!.applicationContext, sectionItemSpinner, items, itemNmbr,
                        object : SpinnerHelper.SelectionListener<ItemDTO> {
                            override fun onItemSelected(position: Int, item: ItemDTO) {
                                selectedProjectitem = item
                            }

                        })

                })


            } catch (e: ApiException) {
                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
            } catch (e: NoInternetException) {
                Toast.makeText(context?.applicationContext, e.message!!, Toast.LENGTH_SHORT).show()
            }
        }
    }





}