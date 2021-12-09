package com.example.covimap.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
import com.example.covimap.model.User;
import com.example.covimap.utils.Validator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        mappingUIComponent();
        subscribeEventButton();
    }

    private String phoneNumber;
    private String password;
    private String fullName;
    private String birthday;
    private String gender;

    private void mappingUIComponent() {
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        genderRadioGroup = findViewById(R.id.gender_raido_group);
        textInputLayoutPhoneNumber = findViewById(R.id.textInputPhoneNumber);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutFullName = findViewById(R.id.textInputLayoutFullName);
        textInputLayoutBirthday = findViewById(R.id.textInputLayoutBirthday);
        buttonRegister = findViewById(R.id.buttonRegister);
    }


    private void subscribeEventButton() {
        editTextBirthday.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
            DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
                calendar = Calendar.getInstance();
                calendar.set(year, month, day, 7, 0);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                editTextBirthday.setText(simpleDateFormat.format(calendar.getTime()));
            };
            datePickerDialog.setOnDateSetListener(dateSetListener);
            datePickerDialog.show();
        });

        buttonRegister.setOnClickListener(view -> {
            phoneNumber = editTextPhoneNumber.getText().toString();

            if (!Validator.isPhoneNumber(phoneNumber)) {
                textInputLayoutPhoneNumber.setError("Please enter valid phone number");
                return;
            }

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(phoneNumber);

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        textInputLayoutPhoneNumber.setError("Your account has already existed!");
                        return;
                    }

                    textInputLayoutPhoneNumber.setError(null);

                    if (!validate()) {
                        return;
                    }

                    User user = new User(phoneNumber, password, fullName, birthday, gender);
                    mDatabase.setValue(user);

                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    private Calendar calendar;

    public boolean validate() {
        password = editTextPassword.getText().toString();
        fullName = editTextFullName.getText().toString();
        birthday = editTextBirthday.getText().toString();
        RadioButton radioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());

        if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        if(fullName.isEmpty()){
            textInputLayoutFullName.setError("Please enter your fullname!");
            return false;
        } else {
            textInputLayoutFullName.setError(null);
        }

        if(birthday.isEmpty()){
            textInputLayoutBirthday.setError("Please enter your birthday!");
            return false;
        } else {
            textInputLayoutBirthday.setError(null);
        }

        if (radioButton == null) {
            Snackbar.make(buttonRegister, "Please choose your gender!", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}