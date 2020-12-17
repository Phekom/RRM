package za.co.xisystems.itis_rrm.custom.notifications

import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO

data class ColorToast(
    val title: String? = null,
    val message: String,
    val style: ToastStyle = INFO,
    val gravity: ToastGravity = CENTER,
    val duration: ToastDuration = SHORT
)
