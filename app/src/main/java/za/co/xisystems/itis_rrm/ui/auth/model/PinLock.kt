package za.co.xisystems.itis_rrm.ui.auth.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import za.co.xisystems.itis_rrm.BR

class PinLock : BaseObservable() {
    @get:Bindable
    var lockNumber: String? = null

    @get:Bindable
    var valid = false

    @get:Bindable
    var registered = false

    fun setLockNumber(value: String?): PinLock {
        this.lockNumber = value
        notifyPropertyChanged(BR.lockNumber)
        return this
    }

    fun setValid(valid: Boolean): PinLock {
        this.valid = valid
        return this
    }

    fun setRegistered(registered: Boolean): PinLock {
        this.registered = registered
        return this
    }
}
