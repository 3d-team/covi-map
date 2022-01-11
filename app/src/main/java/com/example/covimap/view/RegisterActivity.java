package com.example.covimap.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
import com.example.covimap.model.User;
import com.example.covimap.utils.Validator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextPhoneNumber;
    private EditText editTextPassword;
    private EditText editTextFullName;
    private EditText editTextBirthday;
    private RadioGroup genderRadioGroup;

    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutFullName;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutBirthday;

    private Calendar calendar;

    private Button buttonRegister;

    private String phoneNumber;
    private String password;
    private String fullName;
    private String birthday;
    private String gender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mappingUIComponent();
        subscribeEventButton();
    }

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
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.TAIWAN);
                editTextBirthday.setText(simpleDateFormat.format(calendar.getTime()));
            };
            datePickerDialog.setOnDateSetListener(dateSetListener);
            datePickerDialog.show();
        });

        buttonRegister.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        if (!validate()) {
            return;
        }

        registerNewAccount();
    }

    public boolean validate() {
        password = editTextPassword.getText().toString();
        if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        fullName = editTextFullName.getText().toString();
        if(fullName.isEmpty()){
            textInputLayoutFullName.setError("Please enter your fullname!");
            return false;
        } else {
            textInputLayoutFullName.setError(null);
        }

        birthday = editTextBirthday.getText().toString();
        if(birthday.isEmpty()){
            textInputLayoutBirthday.setError("Please enter your birthday!");
            return false;
        } else {
            textInputLayoutBirthday.setError(null);
        }

        RadioButton radioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
        if (radioButton == null) {
            Snackbar.make(buttonRegister, "Please choose your gender!", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void registerNewAccount() {
        phoneNumber = editTextPhoneNumber.getText().toString();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textInputLayoutPhoneNumber.setError("Your account has already existed!");
                    return;
                }
                textInputLayoutPhoneNumber.setError(null);

                createNewUserOnFirebase();
                notifyLoginAgain();
                finish();
            }

            private void createNewUserOnFirebase() {
                String hashedPassword = hashingPassword();
                User user = new User(phoneNumber, hashedPassword, fullName, birthday, gender);
                mDatabase.setValue(user);
            }

            private String hashingPassword() {
                return Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
            }

            private void notifyLoginAgain() {
                Toast.makeText(RegisterActivity.this,
                        "Register successfully. Please login again!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}