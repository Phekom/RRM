package za.co.xisystems.itis_rrm.utils.enums

import www.sanju.motiontoast.MotionToast

enum class ToastGravity(private val value: Int) {
    TOP(MotionToast.GRAVITY_TOP),
    BOTTOM(MotionToast.GRAVITY_BOTTOM),
    CENTER(MotionToast.GRAVITY_CENTER);

    fun getValue(): Int {
        return value
    }
}
