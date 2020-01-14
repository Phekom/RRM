package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils;


public abstract class AsyncTaskActivityHelper<T> {
    protected T activity;

    public AsyncTaskActivityHelper(T activity) {
        this.activity = activity;
    }

//    public void toast(String string) {
//        activity.toast(string);
//    }
//
//    public void toast(int string) {
//        activity.toast(string);
//    }
//
//    public void dismissProgressDialog() {
//        activity.dismissProgressDialog();
//    }
//
//    public void showProgressDialog(String... strings) {
//        activity.showProgressDialog(strings);
//    }
//
//    public void showProgressDialog() {
//        activity.showProgressDialog();
//    }
//
//    public String getString(int resId) {
//        return activity.getString(resId);
//    }

    public abstract  void onCreate();
    public abstract void onDestroy();

    public T getActivity(){
        return activity;
    }
}