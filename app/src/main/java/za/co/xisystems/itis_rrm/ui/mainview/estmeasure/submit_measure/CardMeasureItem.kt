package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.Dialog
import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.launch
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.MeasureEstimateListItemBinding
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File

open class CardMeasureItem(
    val activity: FragmentActivity?,
    val itemMeasureId: String,
    val qty: String,
    val rate: String,
    val text: String,
    val measureViewModel: MeasureViewModel,
    var uiScope: UiLifecycleScope
) : BindableItem<MeasureEstimateListItemBinding>() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.measure_estimate_list_item

    override fun bind(viewBinding: MeasureEstimateListItemBinding, position: Int) {
        viewBinding.apply {
            jobNo.text = text
            measureEstimationQtyTextView.text = qty
            measureEstimationLineAmount.text = activity?.getString(R.string.pair, "R", rate)

            measurementsPhotoImage.setOnClickListener {
                Coroutines.main {
                    val measurePhoto =
                        measureViewModel.getJobMeasureItemsPhotoPath(itemMeasureId)
                    showZoomedImage(measurePhoto[0])
                }
            }

            updateMeasureImage()
        }

        viewBinding.root.setOnLongClickListener {
            Coroutines.main {
                measureViewModel.deleteItemMeasureFromList(itemMeasureId)
                measureViewModel.deleteItemMeasurePhotoFromList(itemMeasureId)
            }
            it.isLongClickable
        }
    }

    private fun MeasureEstimateListItemBinding.updateMeasureImage() {
        Coroutines.main {

            val photoPaths = measureViewModel.getJobMeasureItemsPhotoPath(itemMeasureId)

            if (!photoPaths.isNullOrEmpty()) {
                val measurePhoto = photoPaths[0]

                GlideApp.with(this.root.context)
                    .load(Uri.fromFile(File(measurePhoto)))
                    .placeholder(R.drawable.logo_new_medium)
                    .error(R.drawable.no_image)
                    .into(measurementsPhotoImage)

                measurementsPhotoImage.setOnClickListener { view ->
                    uiScope.launch(uiScope.coroutineContext) {
                        measureViewModel.generateGalleryUI(itemMeasureId)
                    }

                    val directions = SubmitMeasureFragmentDirections
                        .actionSubmitMeasureFragmentToCaptureItemMeasurePhotoFragment(itemMeasureId)

                    Navigation.findNavController(view)
                        .navigate(directions)
                }
            } else {
                GlideApp.with(this.root.rootView.context)
                    .load(R.drawable.logo_new_medium)
                    .error(R.drawable.no_image)
                    .into(measurementsPhotoImage)
            }
        }
    }

    private fun showZoomedImage(imageUrl: String?) {
        val dialog = this.activity?.let { Dialog(it, R.style.dialog_full_screen) }
        if (dialog != null) {
            dialog.setContentView(R.layout.new_job_photo)
            val zoomageView =
                dialog.findViewById<ZoomageView>(R.id.zoomedImage)
            GlideApp.with(this.activity!!)
                .load(imageUrl)
                .into(zoomageView)
            dialog.show()
        }
    }


    override fun initializeViewBinding(view: View): MeasureEstimateListItemBinding {
        return MeasureEstimateListItemBinding.bind(view)
    }
}
