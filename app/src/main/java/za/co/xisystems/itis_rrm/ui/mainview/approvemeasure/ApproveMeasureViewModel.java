package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Francis Mahlava on 03,October,2019
 */
public class ApproveMeasureViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ApproveMeasureViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Measurements Approval fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}