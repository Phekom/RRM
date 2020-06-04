package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import android.os.Bundle
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
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.UnSubmittedJobItem
import za.co.xisystems.itis_rrm.utils.*

class UnSubmittedFragment : BaseFragment( R.layout.fragment_unsubmittedjobs), KodeinAware {
//
    override val kodein by kodein()
    private lateinit var unsubmittedViewModel: UnSubmittedViewModel
    private val factory: UnSubmittedViewModelFactory by instance<UnSubmittedViewModelFactory>()
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    companion object {
        val TAG: String = UnSubmittedFragment::class.java.simpleName
    }

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
            ViewModelProvider(this, factory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            try {
                groupAdapter = GroupAdapter()

                val measurements =
                    unsubmittedViewModel.getJobsForActivityId(ActivityIdConstants.JOB_ESTIMATE)

                measurements.observe(viewLifecycleOwner, Observer { job_s ->
                    if (job_s.isEmpty()) {
                        groupAdapter.clear()
                        noData.visibility = View.VISIBLE
                        group12_loading.visibility = View.GONE
                    } else {
                        noData.visibility = View.GONE
                        initRecyclerView(job_s.toApproveListItems())
                        toast(job_s.size.toString())
                        group12_loading.visibility = View.GONE
                    }

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
            }
        }
    }


    private fun List<JobDTO>.toApproveListItems(): List<UnSubmittedJobItem> {
        return this.map { jobsToApprove ->
            UnSubmittedJobItem(jobsToApprove, unsubmittedViewModel, groupAdapter)
        }
    }

    private fun initRecyclerView(items: List<UnSubmittedJobItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }
        incomplete_job_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }

    }

    override fun onDestroyView() {
        incomplete_job_listView.adapter = null
        super.onDestroyView()
    }
}

