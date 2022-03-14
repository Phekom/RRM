package za.co.xisystems.itis_rrm.ui.snapcapture.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.databinding.CarouselFragmentBinding

class CarouselFragment : LocationFragment() {

    companion object {
        fun newInstance() = CarouselFragment()
        val POSITION_KEY = CarouselFragment::class.java.simpleName.toString().plus("position")
    }

    private lateinit var viewModel: CarouselViewModel
    private val factory: CarouselViewModelFactory by instance()
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: CarouselAdapter
    private lateinit var snapHelper: SnapHelper
    private var _ui: CarouselFragmentBinding? = null
    private val ui get() = _ui!!
    private var position: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _ui = CarouselFragmentBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this.requireActivity(), factory)[CarouselViewModel::class.java]
    }

    private fun initRecyclerViewPosition(position: Int) {
        // This initial scroll will be slightly off because it doesn't respect the SnapHelper.
        // Do it anyway so that the target view is laid out, then adjust onPreDraw.
        layoutManager.scrollToPosition(position)

        ui.recyclerView.doOnPreDraw {
            val targetView = layoutManager.findViewByPosition(position) ?: return@doOnPreDraw
            val distanceToFinalSnap =
                snapHelper.calculateDistanceToFinalSnap(layoutManager, targetView)
                    ?: return@doOnPreDraw

            layoutManager.scrollToPositionWithOffset(position, -distanceToFinalSnap[0])
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        val capturedPhotos = viewModel.getUnallocatedPhotos()
        capturedPhotos?.observe(viewLifecycleOwner, { unallocatedPhotos ->
            unallocatedPhotos?.let {
                layoutManager = ProminentLayoutManager(this.requireContext())
                adapter = CarouselAdapter(unallocatedPhotos)
                snapHelper = PagerSnapHelper()
            }
        })

        with(ui.recyclerView) {
            setItemViewCacheSize(4)
            layoutManager = this@CarouselFragment.layoutManager
            adapter = this@CarouselFragment.adapter

            val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
            addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
            addItemDecoration(BoundsOffsetDecoration())

            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle) {
        position = savedInstanceState.getInt(POSITION_KEY, 0)
    }

    override fun onResume() {
        super.onResume()
        initRecyclerViewPosition(position)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putInt(POSITION_KEY, position)
        }
    }
}
