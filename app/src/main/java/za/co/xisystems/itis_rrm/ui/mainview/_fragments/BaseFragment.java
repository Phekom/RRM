package za.co.xisystems.itis_rrm.ui.mainview._fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import za.co.xisystems.itis_rrm.R;


/**
 * Created by Francis Mahlava on 03,October,2019
 */

public class BaseFragment extends Fragment {
//    protected IProgressView progressView;
    protected Animation bounce, shake;
    protected View coordinator;

    @Override
    public void onResume() {
        super.onResume();
//        ViewLogger.logView(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        progressView = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bounce = AnimationUtils.loadAnimation(context, R.anim.bounce);
        shake = AnimationUtils.loadAnimation(context, R.anim.shake);
//        if (context instanceof IProgressView) {
//            progressView = (IProgressView) context;
//        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.coordinator = view.findViewById(R.id.coordinator);
    }

    public void toast(String string) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    public void toast(int string) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    protected void snackError(View coordinator, String string) {
        if (coordinator != null) {
            Snackbar snackbar = Snackbar.make(coordinator, string, 3000);
            snackbar.getView().setBackgroundColor(Color.RED);
            snackbar.getView().startAnimation(shake);
            snackbar.show();
        } else Log.e("x-", "coordinator is null");
    }

    protected void snackError(String string) {
        snackError(this.coordinator, string);
    }
}