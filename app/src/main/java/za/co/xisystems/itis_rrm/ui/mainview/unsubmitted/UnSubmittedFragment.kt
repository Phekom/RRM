package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.FragmentUnsubmittedjobsBinding
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.UnSubmittedJobItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class UnSubmittedFragment : LocationFragment() {

    private lateinit var addViewModel: AddItemsViewModel
    private val createFactory: AddItemsViewModelFactory by instance()
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
                Intent(requireContext(), MainActivity::class.java).also { home ->
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
//                Navigation.findNavController(this@DeclineJobFragment.requireView())
//                    .navigate(R.id.action_global_nav_home)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    private val backClickListener = View.OnClickListener {
        setBackPressed()
    }

    private fun setBackPressed() {
        Intent(requireContext(), MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentUnsubmittedjobsBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setTitle(getString(R.string.menu_unSubmitted))
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        unSubmittedViewModel =
            ViewModelProvider(this.requireActivity(), factory).get(UnSubmittedViewModel::class.java)

        addViewModel =
            ViewModelProvider(this.requireActivity(), createFactory).get(AddItemsViewModel::class.java)
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
        _ui?.group12Loading?.visibility = View.VISIBLE

        Coroutines.main {
            try {
                val measurements =
                    unSubmittedViewModel.getJobsForActivityIds(
                        ActivityIdConstants.JOB_ESTIMATE,
                        ActivityIdConstants.JOB_PENDING_UPLOAD
                    )

                measurements.observe(viewLifecycleOwner) { jobList ->
                    if (jobList.isNullOrEmpty()) {
                        groupAdapter.clear()
                        _ui?.noData?.visibility = View.VISIBLE
                        _ui?.incompleteJobListView?.visibility = View.GONE
                    } else {
                        _ui?.noData?.visibility = View.GONE
                        _ui?.incompleteJobListView?.visibility = View.VISIBLE
                        initRecyclerView(jobList.toUnSubmittedListItems())
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t, "Failed to fetch unsubmitted jobs!")
                val unsubError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)

                crashGuard(
                    throwable = unsubError,
                    refreshAction = { this@UnSubmittedFragment.retryUnsubmitted() }
                )
            } finally {
                _ui?.group12Loading?.visibility = View.GONE
            }
        }
    }

    fun retryUnsubmitted() {
        IndefiniteSnackbar.hide()
        fetchUnsubmitted()
    }

    private fun List<JobDTO>.toUnSubmittedListItems(): List<UnSubmittedJobItem> {
        return this.map { jobsToApprove ->
            UnSubmittedJobItem(
                jobsToApprove,
                unSubmittedViewModel,
                addViewModel,
                groupAdapter,
                this@UnSubmittedFragment//, currentLocation
            )
        }
    }

    private fun initRecyclerView(items: List<UnSubmittedJobItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>().apply {
            update(items)
        }
        _ui?.incompleteJobListView?.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        _ui?.incompleteJobListView?.adapter = null
        _ui = null
        super.onDestroyView()
    }
}
