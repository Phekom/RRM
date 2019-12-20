package za.co.xisystems.itis_rrm.utils;

import android.app.Activity;
import android.util.Log;

import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment;

public class ViewLogger {
    public static void logView(Object view) {
        if (view != null) {
            if (view instanceof BaseFragment) {
                BaseFragment baseFragment = (BaseFragment) view;
                Activity activity = baseFragment.getActivity();
                if (activity != null)
                    Log.d("x-class", activity.getClass().getSimpleName() + " > " + view.getClass().getSimpleName());
                else
                    Log.d("x-class", view.getClass().getSimpleName());
            } else {
                Log.d("x-class", view.getClass().getSimpleName());
            }
        }
    }
}
