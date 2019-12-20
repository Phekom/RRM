package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils;

/**
 * Created by Pieter Jacob on 2016/08/18.
 * Updated by Pieter Jacobs during 2016/08.
 */
public interface HorizontalProgressBar {

    void showHorizontalProgressDialog(CharSequence message);

    void setProgressBarMessage(CharSequence message);

//    void stepProgressDialogTo(int num);

    void dismissProgressDialog();
}
