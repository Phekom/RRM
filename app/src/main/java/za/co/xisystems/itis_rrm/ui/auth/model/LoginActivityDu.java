package za.co.xisystems.itis_rrm.ui.auth.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import za.co.xisystems.itis_rrm.MainActivity;
import za.co.xisystems.itis_rrm.R;
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase;
import za.co.xisystems.itis_rrm.data.repositories.UserRepository;
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding;
import za.co.xisystems.itis_rrm.ui.auth.AuthViewModel;

public class LoginActivityDu extends AppCompatActivity implements View.OnClickListener{

    private ActivityLoginBinding activityPinLockBinding;
    private PinLock pin = new PinLock();
    private String pinInput = "";
    private Integer index = 0;
    private AppDatabase Db;
    private AuthViewModel loginViewModel;
    private UserRepository repository;
//    private UserDTO userDTO = new UserDTO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPinLockBinding = DataBindingUtil.setContentView(this,R.layout.activity_login);
//         loginViewModel = ViewModelProviders.of(this).get(AuthViewModel.class);


        initPin();
        initListener();



    }

    private void initPin(){
//        String newpin =  loginViewModel.getPin();
//        if(loginViewModel.getPin() != null) {
////            pin = usr.getPin();
//            Toast.makeText(this, newpin, Toast.LENGTH_SHORT).show();
//        }
    }

    private void checkPinColor(){
        if(index == 1) {
            activityPinLockBinding.pin1.setImageResource(R.drawable.oval_pin_green);
        } else if(index == 2) {
            activityPinLockBinding.pin2.setImageResource(R.drawable.oval_pin_green);
        } else if(index == 3) {
            activityPinLockBinding.pin3.setImageResource(R.drawable.oval_pin_green);
        } else if(index == 4) {
            activityPinLockBinding.pin4.setImageResource(R.drawable.oval_pin_green);
        }
    }

    private void initListener() {
        activityPinLockBinding.btn0.setOnClickListener(this);
        activityPinLockBinding.btn1.setOnClickListener(this);
        activityPinLockBinding.btn2.setOnClickListener(this);
        activityPinLockBinding.btn3.setOnClickListener(this);
        activityPinLockBinding.btn4.setOnClickListener(this);
        activityPinLockBinding.btn5.setOnClickListener(this);
        activityPinLockBinding.btn6.setOnClickListener(this);
        activityPinLockBinding.btn7.setOnClickListener(this);
        activityPinLockBinding.btn8.setOnClickListener(this);
        activityPinLockBinding.btn9.setOnClickListener(this);
        activityPinLockBinding.btnCancel.setOnClickListener(this);
        activityPinLockBinding.btnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == activityPinLockBinding.btn0) {
            pinInput += "0";
            index++;
        }
        else if(v == activityPinLockBinding.btn1) {
            pinInput += "1";
            index++;
        }
        else if(v == activityPinLockBinding.btn2) {
            pinInput += "2";
            index++;
        }
        else if(v == activityPinLockBinding.btn3) {
            pinInput += "3";
            index++;
        }
        else if(v == activityPinLockBinding.btn4) {
            pinInput += "4";
            index++;
        }
        else if(v == activityPinLockBinding.btn5) {
            pinInput += "5";
            index++;
        }
        else if(v == activityPinLockBinding.btn6) {
            pinInput += "6";
            index++;
        }
        else if(v == activityPinLockBinding.btn7) {
            pinInput += "7";
            index++;
        }
        else if(v == activityPinLockBinding.btn8) {
            pinInput += "8";
            index++;
        }
        else if(v == activityPinLockBinding.btn9) {
            pinInput += "9";
            index++;
        }
        else if(v == activityPinLockBinding.btnCancel) {
            reset();
        }
        else if(v == activityPinLockBinding.btnDelete) {
            if(index == 0){

            } else {
                if(index == 1) {
                    activityPinLockBinding.pin1.setImageResource(R.drawable.oval_pin_grey);
                } else if(index == 2) {
                    activityPinLockBinding.pin2.setImageResource(R.drawable.oval_pin_grey);
                } else if(index == 3) {
                    activityPinLockBinding.pin3.setImageResource(R.drawable.oval_pin_grey);
                } else if(index == 4) {
                    activityPinLockBinding.pin4.setImageResource(R.drawable.oval_pin_grey);
                }
                pinInput = pinInput.substring(0, pinInput.length()-1);
                index--;
            }
        }
        Log.d("<TEST>", "Masuk" + index);
        checkPin();
        checkPinColor();
    }

    private void checkPin() {
        if(index == 4){
            if(!pin.getRegistered()) {
//                db.insertPin(pinInput);
                Toast.makeText(this, "Pin Successfully registered", Toast.LENGTH_SHORT).show();
                gotoMainActivity();
            }else{
                validatePin();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }

    private void gotoMainActivity() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        reset();
    }

    private void validatePin() {
        if(pin.getLockNumber().equals(pinInput)){
            pin.setValid(true);
        } else {
            pin.setValid(false);
        }
        reset();
        showMessage(pin.getValid());
    }

    private void reset() {
        index = 0;
        pinInput = "";
        resetAllPinColor();
    }

    private void resetAllPinColor() {
        activityPinLockBinding.pin1.setImageResource(R.drawable.oval_pin_grey);
        activityPinLockBinding.pin2.setImageResource(R.drawable.oval_pin_grey);
        activityPinLockBinding.pin3.setImageResource(R.drawable.oval_pin_grey);
        activityPinLockBinding.pin4.setImageResource(R.drawable.oval_pin_grey);
    }

    private void showMessage(boolean valid) {
        if(valid) {
            gotoMainActivity();
        } else {
            Toast.makeText(this, "Pin is incorrect", Toast.LENGTH_SHORT).show();
            resetAllPinColor();
            pinInput = "";
        }
    }



}
