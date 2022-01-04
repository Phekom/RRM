package za.co.xisystems.itis_rrm.custom.notifications

import www.sanju.motiontoast.MotionToastStyle

enum class ToastStyle(private val value: MotionToastStyle) {
    SUCCESS(MotionToastStyle.SUCCESS),
    ERROR(MotionToastStyle.ERROR),
    NO_INTERNET(MotionToastStyle.NO_INTERNET),
    WARNING(MotionToastStyle.WARNING),
    INFO(MotionToastStyle.INFO),
    DELETE(MotionToastStyle.DELETE);

    fun getValue(): MotionToastStyle {
        return value
    }
}
