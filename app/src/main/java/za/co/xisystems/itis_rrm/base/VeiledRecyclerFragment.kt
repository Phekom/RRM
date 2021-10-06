package za.co.xisystems.itis_rrm.base

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.skydoves.androidveil.VeilRecyclerFrameView
import com.skydoves.androidveil.VeiledItemOnClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard

abstract class VeiledRecyclerFragment : BaseFragment() {

    /**
     * Extension function to set up VeilRecyclerFrameView for display
     * @receiver VeilRecyclerFrameView
     * @param layoutResId Int -  veiled item layout
     * @param context Context - fragment context or activity
     * @param adapter GroupAdapter<GroupieViewHolder> - groupie adapter
     * @param veiledItemsToLoad Int - number of veiled items to load
     */
    protected fun VeilRecyclerFrameView.initVeil(
        layoutResId: Int,
        context: Context,
        adapter: GroupAdapter<GroupieViewHolder>,
        veiledItemsToLoad: Int
    ) {
        this.run {

            setVeilLayout(
                layoutResId,
                object : VeiledItemOnClickListener {
                    /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
                    override fun onItemClicked(pos: Int) {
                        Toast.makeText(context, "Loading ...", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            setAdapter(adapter)
            setLayoutManager(LinearLayoutManager(context))
            addVeiledItems(veiledItemsToLoad)
        }
    }

    private fun VeilRecyclerFrameView.delayedUnveil(delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (!activity?.isFinishing!!) {
                    unVeil()
                }
            },
            delay
        )
    }

    protected suspend fun protectedFetch(
        veiled: Boolean = false,
        fetchQuery: suspend () -> Unit = {},
        retryAction: () -> Unit = {},
        veilRecyclerFrameView: VeilRecyclerFrameView
    ) {
        try {
            if (veiled) {
                veilRecyclerFrameView.veil()
            }
            fetchQuery()
        } catch (t: Throwable) {
            veilRecyclerFrameView.unVeil()
            val xiFail = XIResult.Error(exception = t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = xiFail,
                refreshAction = { retryAction() }
            )
        } finally {
            if (veiled) veilRecyclerFrameView.delayedUnveil(Constants.ONE_SECOND)
        }
    }
}
