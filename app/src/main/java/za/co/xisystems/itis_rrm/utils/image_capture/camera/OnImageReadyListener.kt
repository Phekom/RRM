package za.co.xisystems.itis_rrm.utils.image_capture.camera

import za.co.xisystems.itis_rrm.utils.image_capture.model.Image

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
interface OnImageReadyListener {
    fun onImageReady(images: ArrayList<Image>)
    fun onImageNotReady()
}