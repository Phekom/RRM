package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import za.co.xisystems.itis_rrm.R;

public class ApproveMeasureFragment extends Fragment {

    private ApproveMeasureViewModel approveMeasureViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        approveMeasureViewModel =
                ViewModelProviders.of(this).get(ApproveMeasureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_approvemeasure, container, false);
//        final TextView textView = root.findViewById(R.id.text_approval_measure);
        approveMeasureViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });
        return root;
    }
}