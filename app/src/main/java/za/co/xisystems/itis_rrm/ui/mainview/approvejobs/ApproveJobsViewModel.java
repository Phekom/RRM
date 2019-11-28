package za.co.xisystems.itis_rrm.ui.mainview.approvejobs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Francis Mahlava on 03,October,2019
 */
public class ApproveJobsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ApproveJobsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is JobDTO Approval fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}