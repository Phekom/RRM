package za.co.xisystems.itis_rrm.utils.enums

import www.sanju.motiontoast.MotionToast

enum class ToastDuration(private val value: Long) {
    LONG(MotionToast.LONG_DURATION),
    SHORT(MotionToast.SHORT_DURATION);

    fun getValue(): Long {
        return value
    }
}
