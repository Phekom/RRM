package za.co.xisystems.itis_rrm.utils.image_capture

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.ItemExifDataBinding
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.ExifTagsContainer

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class DamageImagePickerItem(
    val imageItem: ExifTagsContainer,
    val activity: ImagePickerActivity,
    val imagepath: String,
    val imageData: RecyclerView

) : BindableItem<ItemExifDataBinding>() {
    override fun getLayout() = R.layout.item_exif_data
    override fun initializeViewBinding(view: View): ItemExifDataBinding {
        return ItemExifDataBinding.bind(view)
    }

    var clickListener: ((DamageImagePickerItem) -> Unit)? = null

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun bind(_binding: ItemExifDataBinding, position: Int) {
        _binding.apply {
            Coroutines.main {

                textProperties.text = imageItem.getOnStringProperties()

                when (imageItem.type) {
//                    Type.GPS -> {
//                        imageType.setImageResource(R.drawable.ic_pin_drop)
//                        textType.text = activity.getString(R.string.item_location)
//                    }
//                    Type.DATE -> {
//                        imageType.setImageResource(R.drawable.ic_date_range)
//                        textType.text = activity.getString(R.string.item_date)
//                    }
//
//                    Type.CAMERA_PROPERTIES -> {
//                        imageType.setImageResource(R.drawable.ic_photo_camera)
//                        textType.text = activity.getString(R.string.item_camera_properties)
//                    }
//                    Type.DIMENSION -> {
//                        imageType.setImageResource(R.drawable.ic_photo_size_select_actual)
//                        textType.text = activity.getString(R.string.item_dimension)
//                    }
//                    Type.OTHER -> {
//                        imageType.setImageResource(R.drawable.ic_blur_on)
//                        textType.text = activity.getString(R.string.item_other)
//                    }
                    else -> {}
                }

            }
        }
        _binding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
//            activity.showAlertDialog(imageItem, activity,imagepath, imageData)
        }
    }


    private fun getItemId(position: Int): Long {
        return position.toLong()
    }

}




