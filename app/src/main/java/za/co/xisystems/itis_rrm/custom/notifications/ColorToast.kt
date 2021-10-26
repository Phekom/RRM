package za.co.xisystems.itis_rrm.custom.notifications

import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.CENTER

data class ColorToast(
    val title: String? = null,
    val message: String,
    val style: ToastStyle = ToastStyle.INFO,
    val gravity: ToastGravity = CENTER,
    val duration: ToastDuration = SHORT
)
