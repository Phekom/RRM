package za.co.xisystems.itis_rrm.utils.image_capture.listener

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
internal interface ResultListener<T> {

    fun onResult(t: T?)
}
