package za.co.xisystems.itis_rrm.utils.image_capture

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageHelper
import za.co.xisystems.itis_rrm.utils.image_capture.model.CallbackStatus
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImageResult
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * Created by Francis Mahlava on 2021/11/23.
 */

class ImagePickerViewModel(private val unallocatedRepository: CapturedPictureRepository, application: Application, photoUtil: PhotoUtil) : AndroidViewModel(application) {

    private val contextRef = WeakReference(application.applicationContext)
    private lateinit var config: ImagePickerConfig
    private var job: Job? = null
    private var photoUtil = photoUtil

    lateinit var selectedImages: MutableLiveData<ArrayList<Image>>
    val result = MutableLiveData(ImageResult(CallbackStatus.IDLE, arrayListOf()))

    fun setConfig(config: ImagePickerConfig) {
        this.config = config
        selectedImages = MutableLiveData(config.selectedImages)
    }

    fun getConfig() = config

    fun fetchImages(activity: ImagePickerActivity) {
        if (job != null) return

        result.postValue(ImageResult(CallbackStatus.FETCHING, arrayListOf()))
        job = viewModelScope.launch() {
            try {
                val images = fetchImagesFromExternalStorage(activity)

                result.postValue(ImageResult(CallbackStatus.SUCCESS, images))
            } catch (e: Exception) {
                result.postValue(ImageResult(CallbackStatus.SUCCESS, arrayListOf()))
            } finally {
                job = null
            }
        }
    }



    private suspend fun fetchImagesFromExternalStorage(activity: ImagePickerActivity): ArrayList<Image> {
        if (contextRef.get() == null) return arrayListOf()

        return withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )

             val imageCollectionUri = ImageHelper.getImageCollectionUri(photoUtil)

            contextRef.get()!!.contentResolver.query(
                imageCollectionUri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            )?.use { cursor ->
                val images = arrayListOf<Image>()

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketIdColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                    val bucketName = cursor.getString(bucketNameColumn)

                    val uri = ContentUris.withAppendedId(imageCollectionUri, id)
                    if (bucketName.equals("RRM App Photos")){
                        val image = Image(uri, name, bucketId, bucketName)
                        images.add(image)
                    }else{
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val image = Image(contentUri, name, bucketId, bucketName)
                        images.add(image)
                    }

//                    val image = Image(uri, name, bucketId, bucketName)
//                    images.add(image)
                }
                cursor.close()
                images
            } ?: throw IOException()
        }
    }


}