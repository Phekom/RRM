package za.co.xisystems.itis_rrm.custom.notifications

import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle

data class ColorToast(
    val message: String,
    val style: ToastStyle,
    val gravity: ToastGravity,
    val duration: ToastDuration
)
