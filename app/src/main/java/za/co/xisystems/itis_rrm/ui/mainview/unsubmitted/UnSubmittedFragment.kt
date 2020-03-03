package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvemeasure.noData
import kotlinx.android.synthetic.main.fragment_unsubmittedjobs.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.UnSubmitedJob_Item
import za.co.xisystems.itis_rrm.utils.*

class UnSubmittedFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private val factory: UnSubmittedViewModelFactory by instance()
    private lateinit var groupAdapter : GroupAdapter<GroupieViewHolder>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unsubmittedjobs, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        unsubmittedViewModel = activity?.run {
            val get = ViewModelProvider(this, factory).get(UnSubmittedViewModel::class.java)
            get
        } ?: throw Exception("Invalid Activity")
        Coroutines.main {
            try {
                groupAdapter = GroupAdapter()
                //            mydata_loading.show()
                val measurements =
                    unsubmittedViewModel.getJobsForActivityId(ActivityIdConstants.JOB_ESTIMATE)
//            val measurements = approveViewModel.offlinedata.await()
                measurements.observe(viewLifecycleOwner, Observer { job_s ->
                    if (job_s.isEmpty()) {
                        noData.visibility = View.VISIBLE
                        group12_loading.visibility = View.GONE
                    } else {
                        noData.visibility = View.GONE
                        initRecyclerView(job_s.toApproveListItems())
                        toast(job_s.size.toString())
                        group12_loading.visibility = View.GONE
                    }

                })
            }  catch (e: ApiException) {
                ToastUtils().toastLong(activity, e.message)
                Log.e("Service-Host", "API Exception", e)
            } catch (e: NoInternetException) {
                ToastUtils().toastLong(activity, e.message)
                Log.e("Network-Connection", "No Internet Connection", e)
            } catch (e: NoConnectivityException) {
                ToastUtils().toastLong(activity, e.message)
                Log.e("Network-Error", "Service Host Unreachable", e)
            }
        }
    }


    private fun List<JobDTO>.toApproveListItems(): List<UnSubmitedJob_Item> {
        return this.map { approvej_items ->
            UnSubmitedJob_Item(approvej_items, unsubmittedViewModel, activity, groupAdapter)
        }
    }

    private fun initRecyclerView(items: List<UnSubmitedJob_Item>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }
        incomplete_job_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }

    }


}

