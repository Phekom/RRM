package za.co.xisystems.itis_rrm.ui.mainview.work

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.noData
import kotlinx.android.synthetic.main.fragment_work.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.ApiException
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.ui.models.WorkViewModel
import za.co.xisystems.itis_rrm.ui.models.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import java.util.concurrent.CancellationException


const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(R.layout.fragment_work), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()
    private var uiScope = UiLifecycleScope()
    private lateinit var dialog: ProgressDialog

    init {
        lifecycleScope.launch {

            whenStarted {
                uiScope.onCreate()
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
                dialog =
                    setDataProgressDialog(
                        requireActivity(),
                        getString(R.string.data_loading_please_wait)
                    )

                uiScope.launch(coroutineContext) {
                    try {
                        val works = workViewModel.getJobsForActivityId(
                            ActivityIdConstants.JOB_APPROVED,
                            ActivityIdConstants.ESTIMATE_INCOMPLETE
                        )

                        works.observe(viewLifecycleOwner, Observer { work_s ->
                            noData.visibility = View.GONE
                            group7_loading.visibility = View.GONE
                            val headerItems = work_s.distinctBy {
                                it.JobId
                            }
                            initRecyclerView(headerItems.toWorkListItems())
                            dialog.dismiss()

                        })
                    } catch (e: ApiException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "API Exception")
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "No Internet Connection")
                    } catch (e: NoConnectivityException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "Service Host Unreachable")
                    } finally {
                        dialog.dismiss()
                    }

                }
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        workViewModel = activity?.run {
            ViewModelProvider(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")



        works_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        works_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        works_swipe_to_refresh.setOnRefreshListener {
            dialog.show()
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    val jobs = workViewModel.offlineUserTaskList.await()
                    jobs.observe(viewLifecycleOwner, Observer { works ->
                        if (works.isEmpty()) {
                            noData.visibility = View.VISIBLE
                        }
                        works_swipe_to_refresh.isRefreshing = false
                        dialog.dismiss()
                    })
                } catch (e: ApiException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, "API Exception")
                } catch (e: NoInternetException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, "No Internet Connection")
                } catch (e: NoConnectivityException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, "Service Host Unreachable")
                } finally {
                    dialog.dismiss()
                }
            }
        }
    }


    private fun initRecyclerView(
        workListItems: List<ExpandableGroup>
    ) {

        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(workListItems)
        }
        work_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            work_listView.itemAnimator = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)

        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false

    }

    override fun onStop() {
        uiScope.cancel(CancellationException("onStop"))
        super.onStop()

    }

    override fun onDestroyView() {
        works_swipe_to_refresh?.setOnRefreshListener { null }
        work_listView?.adapter = null
        super.onDestroyView()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    //    private fun List<JobItemEstimateDTO>.toWorkListItems(): List<ExpandableGroup> {
    private fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        //Initialize Expandable group with expandable item and specify whether it should be expanded by default or not

        return this.map { work_items ->

            val expandableHeaderItem =
                ExpandableHeaderWorkItem(activity, work_items, workViewModel)
            ExpandableGroup(expandableHeaderItem, false).apply {
                uiScope.launch(uiScope.coroutineContext) {
                    //ESTIMATE_WORK_PART_COMPLETE

                    val estimates = workViewModel.getJobEstimationItemsForJobId(
                        work_items.JobId,
                        ActivityIdConstants.ESTIMATE_INCOMPLETE
                    )
                    estimates.observe(viewLifecycleOwner, Observer { estimateItems ->
                        uiScope.launch(uiScope.coroutineContext) {
                            for (item in estimateItems) {
                                withContext(uiScope.coroutineContext) {
                                    try {

                                        val desc =
                                            workViewModel.getDescForProjectItemId(item.projectItemId!!)

                                        val qty = item.qty.toString()
                                        val rate = item.lineRate.toString()
                                        val estimateId = item.estimateId
                                        add(
                                            CardItem(
                                                activity, desc, qty,
                                                rate,
                                                estimateId, workViewModel, item, work_items
                                            )
                                        )

                                    } catch (exception: Exception) {
                                        Timber.e(exception)
                                    }
                                }

                            }
                        }
                    })

                }
            }
        }
    }
}


