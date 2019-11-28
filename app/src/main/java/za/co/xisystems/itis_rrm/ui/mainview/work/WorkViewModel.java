package za.co.xisystems.itis_rrm.ui.mainview.work;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WorkViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public WorkViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Work fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}