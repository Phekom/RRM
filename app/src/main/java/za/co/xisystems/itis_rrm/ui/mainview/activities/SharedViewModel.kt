package za.co.xisystems.itis_rrm.ui.mainview.activities

//
// Created by Shaun McDonald on 2020/03/11.
//

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val message: MutableLiveData<*> = MutableLiveData<Any?>()
    val progressCaption: MutableLiveData<String?> = MutableLiveData()
    var longRunning: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setMessage(msg: String?) {
        message.value = msg
    }

    fun toggleLongRunning(toggle: Boolean) {
        longRunning.value = toggle
    }

    fun setProgressCaption(caption: String?) {
        progressCaption.value = caption
    }

}