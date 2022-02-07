package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.net.Uri
import android.provider.MediaStore
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
object ImageHelper {

    fun singleListFromImage(image: Image): ArrayList<Image> {
        val images = arrayListOf<Image>()
        images.add(image)
        return images
    }


    fun filterImages(images: ArrayList<Image>, bucketId: Long?): ArrayList<Image> {
        if (bucketId == null || bucketId == 0L) return images

        val filteredImages = arrayListOf<Image>()
        for (image in images) {
            if (image.bucketId == bucketId) {
                filteredImages.add(image)
            }
        }
        return filteredImages
    }

    fun findImageIndex(image: Image, images: ArrayList<Image>): Int {
        for (i in images.indices) {
            if (images[i].uri == image.uri) {
                return i
            }
        }
        return -1
    }

    fun findImageIndexes(subImages: ArrayList<Image>, images: ArrayList<Image>): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        for (image in subImages) {
            for (i in images.indices) {
                if (images[i].uri == image.uri) {
                    indexes.add(i)
                    break
                }
            }
        }
        return indexes
    }


    fun isGifFormat(image: Image): Boolean {
        val fileName = image.name;
        val extension = if (fileName.contains(".")) {
            fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
        } else ""

        return extension.equals("gif", ignoreCase = true)
    }

    fun getImageCollectionUri(): Uri {
        return if (DeviceHelper.isMinSdk29) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}