package za.co.xisystems.itis_rrm.ui.mainview.work

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.noData
import kotlinx.android.synthetic.main.fragment_work.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.utils.*


const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
//        getWorkData()
        Coroutines.main {
            val works = workViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            )
//            val works = workViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVED..ActivityIdConstants.ESTIMATE_INCOMPLETE)
            works.observe(viewLifecycleOwner, Observer { work_s ->
                noData.visibility = View.GONE
                group7_loading.visibility = View.GONE
                val header_items = work_s.distinctBy{
                    it.JobId
                }
                initRecyclerView(header_items.toWorkListItems())
//            initRecyclerView(works.toWorkListItems())

            })

        }
        works_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                context!!.applicationContext,
                R.color.colorPrimary
            )
        )
        works_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        works_swipe_to_refresh.setOnRefreshListener {
            Coroutines.main {
                try {
                    val jobs = workViewModel.offlinedatas.await()
                    jobs.observe(viewLifecycleOwner, Observer { _ ->
                        works_swipe_to_refresh.isRefreshing = false
                    })
                } catch (e: ApiException) {
                    ToastUtils().toastLong(activity, e.message)
                    works_swipe_to_refresh.isRefreshing = false
                    Log.e("Service-Host", "API Exception", e)
                } catch (e: NoInternetException) {
                    ToastUtils().toastLong(activity, e.message)
                    // snackError(this.coordinator, e.message)
                    works_swipe_to_refresh.isRefreshing = false
                    Log.e("Network-Connection", "No Internet Connection", e)
                } catch (e: NoConnectivityException) {
                    ToastUtils().toastLong(activity, e.message)
                    works_swipe_to_refresh.isRefreshing = false
                    Log.e("Network-Error", "Service Host Unreachable", e)
                }

            }
        }

    }

    private fun getWorkData() {
        Coroutines.main {
            val works = workViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            )
//            val works = workViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVED..ActivityIdConstants.ESTIMATE_INCOMPLETE)
            works.observe(viewLifecycleOwner, Observer { work_s ->
                noData.visibility = View.GONE
                group7_loading.visibility = View.GONE
                initRecyclerView(work_s.toWorkListItems())
//            initRecyclerView(works.toWorkListItems())

            })

        }
    }


    private fun initRecyclerView(
        workListItems: List<ExpandableGroup>
    ) {

        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            val header_items = workListItems.distinctBy{
            }
            addAll(workListItems)
        }
        work_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            work_listView.itemAnimator = null
        }

        groupAdapter.setOnItemClickListener { item, view ->

        }

    }

    override fun onResume() {
        super.onResume()
//        getWorkData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
//        if (item2 != null) item2.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




    //    private fun List<JobItemEstimateDTO>.toWorkListItems(): List<ExpandableGroup> {
    private fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        //Initialize Expandable group with expandable item and specify whether it should be expanded by default or not

        return this.map { work_items ->

            val expandableHeaderItem =
                ExpandableHeaderWorkItem(activity, work_items, workViewModel, work_items.JobId)
//            ExpandableHeaderItem("JI:${work_items.JiNo} ", work_items.Descr!! , activity, work_items, workViewModel)
            ExpandableGroup(expandableHeaderItem, false).apply {
                Coroutines.main {
                                                                                                               //ESTIMATE_WORK_PART_COMPLETE
                    val estimates = workViewModel.getJobEstimationItemsForJobId(work_items.JobId,ActivityIdConstants.ESTIMATE_INCOMPLETE)
                    estimates.observe(viewLifecycleOwner, Observer { i_tems ->
                        Coroutines.main {
                                for (item in i_tems) {
                                        Coroutines.main {
                                            val   Desc =
                                                workViewModel?.getDescForProjectItemId(item.projectItemId!!)
                                            val  qty = item.qty.toString()
                                            val  rate = item.lineRate.toString()
                                            val  estimateId = item.estimateId
                                            add(CardItem(  activity, Desc,   qty!!,
                                                rate!!,   estimateId!!,  workViewModel, item  , work_items                                    )
                                            )
//                                add(CardItem( activity, Desc[i].toString(),  workViewModel)).toString()
                                        }

                                }
                            }

                    })

                }
            }
        }

    }


    }


