package za.co.xisystems.itis_rrm.utils;

import android.app.Activity;

import timber.log.Timber;
import za.co.xisystems.itis_rrm.base.BaseFragment;

public class ViewLogger {
    static final String X_CLASS = "x-class";
    public static void logView(Object view) {
        if (view != null) {
            if (view instanceof BaseFragment) {
                BaseFragment baseFragment = (BaseFragment) view;
                Activity activity = baseFragment.getActivity();
                if (activity != null)
                    Timber.tag(X_CLASS).d(activity.getClass().getSimpleName() + " > " + view.getClass().getSimpleName());
                else
                    Timber.tag(X_CLASS).d(view.getClass().getSimpleName());
            } else {
                Timber.tag(X_CLASS).d(view.getClass().getSimpleName());
            }
        }
    }
}
