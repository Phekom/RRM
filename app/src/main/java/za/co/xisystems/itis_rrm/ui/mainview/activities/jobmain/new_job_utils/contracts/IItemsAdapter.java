package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.contracts;

import android.widget.Filter;

public interface IItemsAdapter<T> extends IAdapter<T> {
    Filter getFilter();
}