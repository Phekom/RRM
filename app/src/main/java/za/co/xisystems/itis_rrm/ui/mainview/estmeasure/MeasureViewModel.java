package za.co.xisystems.itis_rrm.ui.mainview.estmeasure;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MeasureViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MeasureViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Measure Estimates fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}