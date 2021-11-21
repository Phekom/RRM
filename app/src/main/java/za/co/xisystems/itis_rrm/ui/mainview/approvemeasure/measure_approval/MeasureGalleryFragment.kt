/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 2:29 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIResult.Error
import za.co.xisystems.itis_rrm.custom.results.XIResult.Success
import za.co.xisystems.itis_rrm.databinding.FragmentMeasureGalleryBinding
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope

class MeasureGalleryFragment : BaseFragment(), DIAware {
    override val di by closestDI()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()
    private val galleryObserver =
        Observer<XIResult<MeasureGalleryUIState>> { handleResponse(it) }
    private var uiScope = UiLifecycleScope()
    private var galleryJobId: String? = null
    private var _ui: FragmentMeasureGalleryBinding? = null
    private val ui get() = _ui!!

    companion object {
        fun newInstance() = MeasureGalleryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.measurement_gallery)
        approveViewModel =
            ViewModelProvider(this.requireActivity(), factory).get(ApproveMeasureViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentMeasureGalleryBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Not all fragments have options
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiScope.launch(uiScope.coroutineContext) {
            approveViewModel.measureGalleryUIState.observe(viewLifecycleOwner, galleryObserver)
        }

        ui.doneImageButton.setOnClickListener {
            val directions = MeasureGalleryFragmentDirections
                .actionMeasureGalleryFragmentToMeasureApprovalFragment(galleryJobId)
            Navigation.findNavController(view)
                .navigate(directions)
        }
    }

    private fun handleResponse(response: XIResult<MeasureGalleryUIState>) {
        when (response) {
            is Success -> {
                val uiState = response.data
                ui.measurementDescription.text = uiState.description
                val costing = "Approved Qty: ${uiState.qty}"
                ui.measurementCosting.text = costing
                ui.estimateImageGalleryView.run {
                    clearImages()
                    addZoomedImages(uiState.photoPairs, this@MeasureGalleryFragment.requireActivity())
                    scaleForSize(uiState.photoPairs.size)
                }
                galleryJobId = uiState.jobItemMeasureDTO?.jobId!!
            }
            is Error ->
                XIErrorHandler.handleError(
                    fragment = this@MeasureGalleryFragment,
                    view = this@MeasureGalleryFragment.requireView(),
                    throwable = response,
                    shouldToast = false,
                    shouldShowSnackBar = true,
                    refreshAction = { retryGallery() }
                )
        }
    }

    private fun retryGallery() {
        //  Not needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.estimateImageGalleryView.clearImages()
        uiScope.destroy()
        _ui = null
    }
}
