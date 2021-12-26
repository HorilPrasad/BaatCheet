package com.byteme.baatcheet.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import com.byteme.baatcheet.R;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    private EditText userPhoneNumber;
    private CountryCodePicker countryCodePicker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //initializing and binding variables

        countryCodePicker = findViewById(R.id.country_code_picker);
        userPhoneNumber = findViewById(R.id.phone_number);
        ImageButton sentOtpButton = findViewById(R.id.sent_otp_btn);

        countryCodePicker.registerCarrierNumberEditText(userPhoneNumber);



        //click event on sent otp button

        sentOtpButton.setOnClickListener(v -> {
            String phoneNumber = countryCodePicker.getFullNumberWithPlus();
            //checking phone number is empty or not
            if (phoneNumber.isEmpty()) {
                userPhoneNumber.setError("Enter Number!");
                userPhoneNumber.requestFocus();
            }
            else {

                //sent to verify otp activity
                Intent intent = new Intent(this,VerifyOtp.class);
                intent.putExtra("phoneNumber",phoneNumber);
                startActivity(intent);
            }
        });
    }
}