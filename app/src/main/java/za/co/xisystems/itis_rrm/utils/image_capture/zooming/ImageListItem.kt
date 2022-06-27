package za.co.xisystems.itis_rrm.utils.image_capture.zooming

import android.graphics.Color
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.ItemImageBinding

/**
 * Created by Francis Mahlava on 2020/03/29.
 */

open class ImageListItem(
//    val inspection: InspectionPhotoDTO,
//    val viewModel: InspectionActivityViewModel,
//    val inspectionActivity: InspectionActivity,
//    val inspectionPhotosList: List<InspectionPhotoDTO>
) : BindableItem<ItemImageBinding>() {

    override fun getLayout() = R.layout.item_image
    override fun initializeViewBinding(view: View): ItemImageBinding {
        return ItemImageBinding.bind(view)
    }


    private val nededColor: Int get() = Color.RED
    var clickListener: ((ImageListItem) -> Unit)? = null
    var longClickListener: ((ImageListItem) -> Unit)? = null
    val from: String = "New Inspection"
    val uriString = ArrayList<String>()

    override fun bind(_binding: ItemImageBinding, position: Int) {
//        inspectionPhotosList.forEach { photo ->
//            uriString.add(photo.photoPath.toString())
//        }
//        _binding.apply {
//            Coroutines.main {
//                appListID.text = za.co.xisystems.rrmsafetyinspection.ui.fragments.add_new_structure.new_structure.getItemId(
//                    position + 1
//                ).toString() + "."
//                val path = inspection.photoPath
//                GlideApp.with(activity)
//                    .load(path)
//                    .into(rrminspectionsimage)
//            }
//        }

        _binding.root.setOnClickListener {
            clickListener?.invoke(this)
            onImageClickAction(uriString, position)
            //ToastUtils().toastShort(activity, "Image " + position)
        }

        _binding.root.setOnLongClickListener {
           // deletePhoto(inspection.photoId)
            longClickListener?.invoke(this)
            return@setOnLongClickListener true
        }

    }


    private fun onImageClickAction(uriString: ArrayList<String>, pos: Int) {
//        val fullImageIntent = Intent(inspectionActivity, ImageViewActivity::class.java)
//        fullImageIntent.putExtra(ImageViewActivity.URI_LIST_DATA, uriString)
//        fullImageIntent.putExtra(ImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, pos)
//        fullImageIntent.putExtra(ImageViewActivity.FROM, from)
//        inspectionActivity.startActivity(fullImageIntent)
    }

//    private fun showZoomedImage(path: String) {
//        val dialog = Dialog(inspectionActivity, R.style.dialog_full_screen)
//        dialog.setContentView(R.layout.large_photo)
//        val zoomageView = dialog.findViewById<ZoomageView>(R.id.zoomedImage)
////        GlideApp.with(inspectionActivity)
//        picasso.load(path)
//            .into(zoomageView)
//        dialog.show()
//    }

    private fun deletePhoto(photoId: String) {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(inspectionActivity)
//        builder.setTitle(R.string.confirm_delete)
//        builder.setMessage(inspectionActivity.getString(R.string.are_you_sure_you_want_to_delete_this_photo))
//
//        builder.setPositiveButton(
//            R.string.confirm
//        ) { dialoginterface, _ ->
//            Coroutines.main {
//                viewModel.deletePhotoForID(photoId)
//                dialoginterface.dismiss()
//            }
//        }
//        builder.setNegativeButton(
//            "Cancel"
//        ) { dialoginterface, _ -> dialoginterface.dismiss() }
//        builder.show()
    }


}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
