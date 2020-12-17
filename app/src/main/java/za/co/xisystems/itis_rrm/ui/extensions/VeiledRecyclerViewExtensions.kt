package za.co.xisystems.itis_rrm.ui.extensions

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.skydoves.androidveil.VeilRecyclerFrameView
import com.skydoves.androidveil.VeiledItemOnClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder

fun VeilRecyclerFrameView.initVeiledRecycler(
    veiledLayoutId: Int,
    context: Context,
    groupAdapter: GroupAdapter<GroupieViewHolder>
) {
    run {
        setVeilLayout(veiledLayoutId, object : VeiledItemOnClickListener {
            /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
            override fun onItemClicked(pos: Int) {
                Toast.makeText(context, "Loading ...", Toast.LENGTH_SHORT).show()
            }
        })
        setAdapter(groupAdapter)
        setLayoutManager(LinearLayoutManager(context))
        addVeiledItems(10)
    }
}
