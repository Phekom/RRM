package za.co.xisystems.itis_rrm.ui.mainview.activities

//
// Created by Shaun McDonald on 2020/03/11.
//

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR

class SharedViewModel(private val userRepository: UserRepository) : ViewModel() {
    val message: MutableLiveData<*> = MutableLiveData<Any?>()
    val progressCaption: MutableLiveData<String> = MutableLiveData()
    var longRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val actionCaption: MutableLiveData<String> = MutableLiveData()
    var originalCaption: String = ""
    val colorMessage: MutableLiveData<ColorToast> = MutableLiveData()
    var takingPhotos: Boolean = false
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

    fun logOut() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.expirePin()
    }
}
