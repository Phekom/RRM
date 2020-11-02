package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts;

public interface IEstimatesAdapter<T> extends IAdapter<T> {
    void enableItemClickListener();

    void disableItemClickListener();

    boolean hasItem(T item);
}