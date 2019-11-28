package za.co.xisystems.itis_rrm.ui.mainview._fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import za.co.xisystems.itis_rrm.R;

/**
 * Created by Mauritz Mollentze in 2015.
 * Update by Pieter Jacobs during 2016/01, 2016/02, 2016/07, 2016/08.
 * Update by Francis Mahlava on 03,October,2019
 */
public class PinFragment extends BaseFragment {
    private OnPinFragmentInteractionListener mListener;
    private EditText pinEditText;
    private TextView pinTextView;

    public PinFragment() {
    }

    public static PinFragment newInstance() {
        PinFragment fragment = new PinFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pin, container, false);

        pinEditText = view.findViewById(R.id.pin_editText);
        pinTextView = view.findViewById(R.id.pinTextView);

        Button oneButton = (Button) view.findViewById(R.id.pin_one_button);
        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_1));
            }
        });
        Button twoButton = (Button) view.findViewById(R.id.pin_two_button);
        twoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_2));
            }
        });
        Button threeButton = (Button) view.findViewById(R.id.pin_three_button);
        threeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_3));
            }
        });
        Button fourButton = (Button) view.findViewById(R.id.pin_four_button);
        fourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_4));
            }
        });
        Button fiveButton = (Button) view.findViewById(R.id.pin_five_button);
        fiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_5));
            }
        });
        Button sixButton = (Button) view.findViewById(R.id.pin_six_button);
        sixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_6));
            }
        });
        Button sevenButton = (Button) view.findViewById(R.id.pin_seven_button);
        sevenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_7));
            }
        });
        Button eightButton = (Button) view.findViewById(R.id.pin_eight_button);
        eightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_8));
            }
        });
        Button nineButton = (Button) view.findViewById(R.id.pin_nine_button);
        nineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_9));
            }
        });
        Button zeroButton = (Button) view.findViewById(R.id.pin_zero_button);
        zeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChanged(getString(R.string.no_0));
            }
        });
        View deleteButton = view.findViewById(R.id.pin_remove_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    String currentNumber = pinEditText.getText().toString();
                    currentNumber = removeLastPinDigit(currentNumber);
                    pinEditText.setText(currentNumber);
                    mListener.onPinChanged(currentNumber);
                    pinTextView.setText(currentNumber.replaceAll("\\d", "*"));
                }
            }
        });
        Button clearButton = (Button) view.findViewById(R.id.pin_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    pinEditText.setText(R.string.empty_string);
                    mListener.onPinChanged("");
                    pinTextView.setText("");
                }
            }
        });
        return view;
    }

    public String removeLastPinDigit(String currentNumber) {
        if (currentNumber.length() != 0){
            return currentNumber.substring(0, currentNumber.length() - 1);
        } else {
            return "";
        }
    }

    public void pinChanged(String number){
        pinEditText.requestFocus();
        if (mListener != null) {
            String currentNumber = pinEditText.getText().toString();
            currentNumber += number;
            pinEditText.setText(currentNumber);
            pinTextView.setText(currentNumber.replaceAll("\\d", "*"));
            mListener.onPinChanged(currentNumber);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPinFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.must_implement_OnPinFragmentInteractionListener));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnPinFragmentInteractionListener {
        void onPinChanged(String pin);
    }


    public void onBackPressed()
    {

    }
}