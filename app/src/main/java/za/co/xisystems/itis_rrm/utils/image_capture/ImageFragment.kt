package za.co.xisystems.itis_rrm.utils.image_capture

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModel
import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModelFactory
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.ImagepickerFragmentBinding
import za.co.xisystems.itis_rrm.utils.image_capture.adapter.ImagePickerAdapter
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageHelper
import za.co.xisystems.itis_rrm.utils.image_capture.helper.LayoutManagerHelper
import za.co.xisystems.itis_rrm.utils.image_capture.listener.OnImageSelectListener
import za.co.xisystems.itis_rrm.utils.image_capture.model.CallbackStatus
import za.co.xisystems.itis_rrm.utils.image_capture.model.GridCount
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImageResult
import za.co.xisystems.itis_rrm.utils.image_capture.widget.GridSpacingItemDecoration

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

class ImageFragment : BaseFragment() {

    private var _binding: ImagepickerFragmentBinding? = null
    private val binding get() = _binding!!

    private var bucketId: Long? = null
    private lateinit var gridCount: GridCount

    private lateinit var viewModel: ImagePickerViewModel
    private val captureFactory: ImagePickerViewModelFactory by instance()
    private lateinit var imageAdapter: ImagePickerAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var itemDecoration: GridSpacingItemDecoration

    companion object {

        const val BUCKET_ID = "BucketId"
        const val GRID_COUNT = "GridCount"

        fun newInstance(bucketId: Long, gridCount: GridCount): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putLong(BUCKET_ID, bucketId)
            args.putParcelable(GRID_COUNT, gridCount)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(gridCount: GridCount): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putParcelable(GRID_COUNT, gridCount)
            fragment.arguments = args
            return fragment
        }
    }

    private val selectedImageObserver = object : Observer<ArrayList<Image>> {
        override fun onChanged(it: ArrayList<Image>) {
            imageAdapter.setSelectedImages(it)
            viewModel.selectedImages.removeObserver(this)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bucketId = arguments?.getLong(BUCKET_ID)
        gridCount = arguments?.getParcelable(GRID_COUNT)!!

        viewModel = requireActivity().run {
            ViewModelProvider(this, captureFactory).get(
                ImagePickerViewModel::class.java
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val config = viewModel.getConfig()

        imageAdapter =
            ImagePickerAdapter(requireActivity(), config, activity as OnImageSelectListener)
        gridLayoutManager = LayoutManagerHelper.newInstance(requireContext(), gridCount)
        itemDecoration = GridSpacingItemDecoration(
            gridLayoutManager.spanCount,
            resources.getDimension(R.dimen.grid_spacing).toInt()
        )

        _binding = ImagepickerFragmentBinding.inflate(inflater, container, false)

        binding.apply {
            root.setBackgroundColor(Color.parseColor(config.backgroundColor))
            progressIndicator.setIndicatorColor(Color.parseColor(config.progressIndicatorColor))
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = gridLayoutManager
                addItemDecoration(itemDecoration)
                adapter = imageAdapter
            }
        }

        viewModel.apply {
            result.observe(viewLifecycleOwner) {
                handleResult(it)
            }
            selectedImages.observe(viewLifecycleOwner, selectedImageObserver)
        }

        return binding.root
    }


    private fun handleResult(result: ImageResult) {
        if (result.status is CallbackStatus.SUCCESS) {
            val images = ImageHelper.filterImages(result.images, bucketId)
            if (images.isNotEmpty()) {
                imageAdapter.setData(images)
                binding.recyclerView.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.GONE
            }
        } else {
            binding.recyclerView.visibility = View.GONE
        }

        binding.apply {
            emptyText.visibility =
                if (result.status is CallbackStatus.SUCCESS && result.images.isEmpty()) View.VISIBLE else View.GONE
            progressIndicator.visibility =
                if (result.status is CallbackStatus.FETCHING) View.VISIBLE else View.GONE
        }
    }

    override fun handleOnConfigurationChanged() {
        val newSpanCount =
            LayoutManagerHelper.getSpanCountForCurrentConfiguration(requireContext(), gridCount)
        itemDecoration =
            GridSpacingItemDecoration(
                gridLayoutManager.spanCount,
                resources.getDimension(R.dimen.grid_spacing).toInt()
            )
        gridLayoutManager.spanCount = newSpanCount
        binding.recyclerView.addItemDecoration(itemDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}