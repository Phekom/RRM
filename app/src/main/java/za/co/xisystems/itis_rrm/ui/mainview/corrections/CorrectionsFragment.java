package za.co.xisystems.itis_rrm.ui.mainview.corrections;

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

public class CorrectionsFragment extends Fragment {

    private CorrectionsViewModel correctionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        correctionsViewModel =
                ViewModelProviders.of(this).get(CorrectionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_correction, container, false);
//        final TextView textView = root.findViewById(R.id.text_tools);
        correctionsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });
        return root;
    }
}