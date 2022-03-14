package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.contracts;

import android.app.Activity;

import java.util.Map;

import za.co.xisystems.itis_rrm.data._commons.views.IBaseActivity;
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO;

public interface INewJobActivity<T extends Activity> extends IBaseActivity<T> {
     void takePhoto(int REQUEST_CODE, Map<String, String> itemInfoMap);
     JobDTO getJob();
     String getString(int resId);
//     Window getWindow();
//     void disableEnableButtons(Boolean delete, Boolean save, Boolean next);
}