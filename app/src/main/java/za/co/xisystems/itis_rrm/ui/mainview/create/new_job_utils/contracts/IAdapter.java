package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public interface IAdapter<T> {
    void setData(List<T> data);

    RecyclerView.Adapter getAdapter();

    void notifyDataSetChanged();

    int getItemCount();
}