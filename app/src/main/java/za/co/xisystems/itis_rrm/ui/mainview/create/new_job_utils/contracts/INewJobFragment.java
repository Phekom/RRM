package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts;

import androidx.fragment.app.Fragment;

import java.util.Map;

import za.co.xisystems.itis_rrm.data._commons.views.IBaseFragment;
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO;

public interface INewJobFragment<T extends Fragment> extends IBaseFragment<T> {
     void takePhoto(int REQUEST_CODE, Map<String, String> itemInfoMap);
     JobDTO getJob();
     String getString(int resId);
//     Window getWindow();
//     void disableEnableButtons(Boolean delete, Boolean save, Boolean next);
}