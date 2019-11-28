package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UnSubmittedViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public UnSubmittedViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Un-Submitted fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}