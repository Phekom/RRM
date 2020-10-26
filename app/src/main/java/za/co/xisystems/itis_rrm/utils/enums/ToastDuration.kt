package za.co.xisystems.itis_rrm.utils.enums

import www.sanju.motiontoast.MotionToast

enum class ToastDuration(private val value: Int) {
    LONG(MotionToast.LONG_DURATION),
    SHORT(MotionToast.SHORT_DURATION);

    fun getValue(): Int {
        return value
    }
}
