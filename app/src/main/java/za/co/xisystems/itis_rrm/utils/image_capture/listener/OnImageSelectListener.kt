package za.co.xisystems.itis_rrm.utils.image_capture.listener

import za.co.xisystems.itis_rrm.utils.image_capture.model.Image

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

interface OnImageSelectListener {
    fun onSelectedImagesChanged(selectedImages: ArrayList<Image>)
    fun onSingleModeImageSelected(image: Image)
}