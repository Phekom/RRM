package za.co.xisystems.itis_rrm.utils.image_capture.model

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
sealed class CallbackStatus {
    object IDLE : CallbackStatus()
    object FETCHING : CallbackStatus()
    object SUCCESS : CallbackStatus()
}