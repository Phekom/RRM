package za.co.xisystems.itis_rrm.ui.auth.model;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class PinLock extends BaseObservable {

    public  String lockNumber;
    public  Boolean isValid = false;
    public  Boolean isRegistered = false;
}
