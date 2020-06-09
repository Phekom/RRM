package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_measure_gallery.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.errors.ErrorHandler.handleError
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.results.XISuccess


class MeasureGalleryFragment : BaseFragment(R.layout.fragment_measure_gallery), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance<ApproveMeasureViewModelFactory>()
    private val galleryObserver =
        Observer<XIResult<ApproveMeasureViewModel.GalleryUIState>> { handleResponse(it) }
    private var uiScope = UiLifecycleScope()

    companion object {
        fun newInstance() = MeasureGalleryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.measurement_gallery)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_measure_gallery, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("Not yet implemented")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        approveViewModel.galleryUIState.observe(viewLifecycleOwner, galleryObserver)

        done_image_button.setOnClickListener { view ->
            Navigation.findNavController(view)
                .navigate(R.id.action_measureGalleryFragment_to_measureApprovalFragment)
        }

    }

    fun handleResponse(response: XIResult<ApproveMeasureViewModel.GalleryUIState>) {
        when (response) {
            is XISuccess -> {
                val uiState = response.data
                measurement_description.text = uiState.description
                val costing = "${uiState.qty} x R ${uiState.lineRate} = R ${uiState.lineAmount}"
                measurement_costing.text = costing
                estimate_image_gallery_view.clearImages()
                estimate_image_gallery_view.addZoomedImages(
                    uiState.photoPaths,
                    this@MeasureGalleryFragment.requireActivity()
                )
                estimate_image_gallery_view.scaleForSize(
                    this@MeasureGalleryFragment.requireContext(),
                    uiState.photoPaths.size
                )
            }
            is XIError ->
                handleError(
                    view = this@MeasureGalleryFragment.requireView(),
                    throwable = response,
                    shouldToast = false,
                    shouldShowSnackBar = true,
                    refreshAction = { retryGallery() }
                )
        }
    }

    private fun retryGallery() {
        TODO("Set up contingency id.")
    }

}


