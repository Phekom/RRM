package za.co.xisystems.itis_rrm.ui.mainview.approvejobs;

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

/**
 * Created by Francis Mahlava on 03,October,2019
 */
public class ApproveJobsFragment extends Fragment {

    private ApproveJobsViewModel approveJobsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        approveJobsViewModel =
                ViewModelProviders.of(this).get(ApproveJobsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_approvejob, container, false);
//        final TextView textView = root.findViewById(R.id.text_approve);
        approveJobsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });
        return root;
    }
}