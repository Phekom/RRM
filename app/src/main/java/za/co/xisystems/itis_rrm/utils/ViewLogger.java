package za.co.xisystems.itis_rrm.utils;

import android.app.Activity;
import android.util.Log;

import timber.log.Timber;
import za.co.xisystems.itis_rrm.base.BaseFragment;

public class ViewLogger {
    public static void logView(Object view) {
        if (view != null) {
            if (view instanceof BaseFragment) {
                BaseFragment baseFragment = (BaseFragment) view;
                Activity activity = baseFragment.getActivity();
                if (activity != null)
                    Timber.tag("x-class").d(activity.getClass().getSimpleName() + " > " + view.getClass().getSimpleName());
                else
                    Timber.tag("x-class").d(view.getClass().getSimpleName());
            } else {
                Timber.tag("x-class").d(view.getClass().getSimpleName());
            }
        }
    }
}
