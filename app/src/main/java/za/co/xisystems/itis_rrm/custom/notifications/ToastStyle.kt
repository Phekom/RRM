package za.co.xisystems.itis_rrm.custom.notifications

import www.sanju.motiontoast.MotionToast

enum class ToastStyle(private val value: String) {
    SUCCESS(MotionToast.TOAST_SUCCESS),
    ERROR(MotionToast.TOAST_ERROR),
    NO_INTERNET(MotionToast.TOAST_NO_INTERNET),
    WARNING(MotionToast.TOAST_WARNING),
    INFO(MotionToast.TOAST_INFO),
    DELETE(MotionToast.TOAST_DELETE);

    fun getValue(): String {
        return value
    }
}
