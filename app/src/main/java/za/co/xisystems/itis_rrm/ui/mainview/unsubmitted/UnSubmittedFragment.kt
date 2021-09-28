package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.FragmentUnsubmittedjobsBinding
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.UnSubmittedJobItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class UnSubmittedFragment : BaseFragment(), DIAware {

    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private val createFactory: CreateViewModelFactory by instance()
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>
    private lateinit var unSubmittedViewModel: UnSubmittedViewModel
    private val factory: UnSubmittedViewModelFactory by instance()
    private var _ui: FragmentUnsubmittedjobsBinding? = null
    private val ui get() = _ui!!

    companion object {
        val TAG: String = UnSubmittedFragment::class.java.canonicalName!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@UnSubmittedFragment.findNavController().popBackStack(R.id.nav_home, false)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentUnsubmittedjobsBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        unSubmittedViewModel = activity?.run {
            ViewModelProvider(this, factory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        createViewModel = activity?.run {
            ViewModelProvider(this, createFactory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to [fragment.requireActivity().onStart] of the containing
     * Activity's lifecycle.
     */
    override fun onStart() {
        super.onStart()
        fetchUnsubmitted()
    }

    private fun fetchUnsubmitted() {
        groupAdapter = GroupAdapter()
        ui.group12Loading.visibility = View.VISIBLE

        Coroutines.main {
            try {
                val measurements =
                    unSubmittedViewModel.getJobsForActivityIds(
                        ActivityIdConstants.JOB_ESTIMATE,
                        ActivityIdConstants.JOB_PENDING_UPLOAD
                    )

                measurements.observe(viewLifecycleOwner, { jobList ->
                    if (jobList.isNullOrEmpty()) {
                        groupAdapter.clear()
                        ui.noData.visibility = View.VISIBLE
                        ui.incompleteJobListView.visibility = View.GONE
                    } else {
                        ui.noData.visibility = View.GONE
                        ui.incompleteJobListView.visibility = View.VISIBLE
                        initRecyclerView(jobList.toApproveListItems())
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Failed to fetch unsubmitted jobs!")
                val unsubError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)

                crashGuard(
                    throwable = unsubError,
                    refreshAction = { this@UnSubmittedFragment.retryUnsubmitted() }
                )
            } finally {
                ui.group12Loading.visibility = View.GONE
            }
        }
    }

    fun retryUnsubmitted() {
        IndefiniteSnackbar.hide()
        fetchUnsubmitted()
    }

    private fun List<JobDTO>.toApproveListItems(): List<UnSubmittedJobItem> {
        return this.map { jobsToApprove ->
            UnSubmittedJobItem(
                jobsToApprove,
                unSubmittedViewModel,
                createViewModel,
                groupAdapter,
                this@UnSubmittedFragment
            )
        }
    }

    private fun initRecyclerView(items: List<UnSubmittedJobItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>().apply {
            update(items)
        }
        ui.incompleteJobListView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        ui.incompleteJobListView.adapter = null
        _ui = null
        super.onDestroyView()
    }
}
