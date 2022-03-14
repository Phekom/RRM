package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.contracts;

import android.app.Activity;

import za.co.xisystems.itis_rrm.data._commons.views.IBaseActivity;
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO;

public interface INewJobEditEstimateActivity<T extends Activity> extends IBaseActivity<T> {
    JobDTO getJob();
}