package za.co.xisystems.itis_rrm.ui.mainview.activities

//
// Created by Shaun McDonald on 2020/03/11.
//

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR

class SharedViewModel : ViewModel() {
    val message: MutableLiveData<*> = MutableLiveData<Any?>()
    val progressCaption: MutableLiveData<String> = MutableLiveData()
    var longRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val actionCaption: MutableLiveData<String> = MutableLiveData()
    var originalCaption: String = ""
    val colorMessage: MutableLiveData<ColorToast> = MutableLiveData()
    fun setMessage(msg: String?) {
        message.value = msg
    }

    fun setColorMessage(
        title: String? = null,
        message: String,
        style: ToastStyle = ERROR,
        position: ToastGravity = CENTER,
        duration: ToastDuration = LONG
    ) {
        val colorToast = ColorToast(title, message, style, position, duration)
        colorMessage.postValue(colorToast)
    }

    fun toggleLongRunning(toggle: Boolean) {
        longRunning.value = toggle
    }

    fun setProgressCaption(caption: String) {
        progressCaption.value = caption
    }

    fun setActionCaption(caption: String) {
        actionCaption.value = caption
    }

    fun resetCaption() {
        actionCaption.value = originalCaption
    }
}
