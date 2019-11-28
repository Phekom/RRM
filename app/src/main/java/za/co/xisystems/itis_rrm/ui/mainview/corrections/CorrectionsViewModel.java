package za.co.xisystems.itis_rrm.ui.mainview.corrections;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CorrectionsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CorrectionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Estimates to be Corrected fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}