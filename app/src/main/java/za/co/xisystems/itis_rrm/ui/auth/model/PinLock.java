package za.co.xisystems.itis_rrm.ui.auth.model;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class PinLock extends BaseObservable {

    public static String lockNumber;
    public static Boolean isValid = false;
    public static Boolean isRegistered = false;

    @Bindable
    public String getLockNumber() {
        return lockNumber;
    }

    public PinLock setLockNumber(String lockNumber) {
        PinLock.lockNumber = lockNumber;
        notifyPropertyChanged(za.co.xisystems.itis_rrm.BR.lockNumber);
        return this;
    }

    @Bindable
    public Boolean getValid() {
        return isValid;
    }

    public PinLock setValid(Boolean valid) {
        isValid = valid;
//        notifyPropertyChanged(BR.valid);
        return this;
    }

    @Bindable
    public Boolean getRegistered() {
        return isRegistered;
    }

    public PinLock setRegistered(Boolean registered) {
        isRegistered = registered;
//        notifyPropertyChanged(BR.registered);
        return this;
    }
}
