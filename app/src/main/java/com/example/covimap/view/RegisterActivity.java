package com.example.covimap.view;

import static com.example.covimap.R.string.phone_number_error;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.covimap.R;
import com.example.covimap.model.User;
import com.example.covimap.utils.SQLiteHelper;
import com.example.covimap.utils.Validator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextPhoneNumber;
    EditText editTextPassword;
    EditText editTextFullName;
    EditText editTextBirthday;
    RadioGroup genderRadioGroup;

    TextInputLayout textInputLayoutPhoneNumber;
    TextInputLayout textInputLayoutFullName;
    TextInputLayout textInputLayoutPassword;
    TextInputLayout textInputLayoutBirthday;

    Button buttonRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initEventButton();
    }

    private String phoneNumber;
    private String password;
    private String fullname;
    private String birthday;
    private String gender;


    private void initViews() {
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextBirthday.setOnClickListener(getBirtday);
        genderRadioGroup = findViewById(R.id.gender_raido_group);

        textInputLayoutPhoneNumber = findViewById(R.id.textInputPhoneNumber);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutFullName = findViewById(R.id.textInputLayoutFullName);
        textInputLayoutBirthday = findViewById(R.id.textInputLayoutBirthday);

        buttonRegister = findViewById(R.id.buttonRegister);
//        initLoginTextView();
    }

//    private void initLoginTextView() {
//        TextView textViewLogin = findViewById(R.id.textViewLogin);
//        textViewLogin.setOnClickListener(view -> finish());
//    }

    private void initEventButton() {
        buttonRegister.setOnClickListener(view -> {
            if (validate()) {

            }
        });
    }

    private Calendar calendar;
    View.OnClickListener getBirtday = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                   calendar = Calendar.getInstance();
                   calendar.set(year, month, day, 7, 0);
                   SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                   editTextBirthday.setText(simpleDateFormat.format(calendar.getTime()));
                }
            };
            datePickerDialog.setOnDateSetListener(dateSetListener);
            datePickerDialog.show();
        }
    };

    public boolean validate() {
        phoneNumber = editTextPhoneNumber.getText().toString();
        password = editTextPassword.getText().toString();
        fullname = editTextFullName.getText().toString();
        birthday = editTextBirthday.getText().toString();
        RadioButton radioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());


        if (!Validator.isPhoneNumber(phoneNumber)) {
//            textInputLayoutPhoneNumber.setError(phone_number_error);
            return false;
        }
        else{textInputLayoutPhoneNumber.setError(null);}

        if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        }

        return true;
    }
}