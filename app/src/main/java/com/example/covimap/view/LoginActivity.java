package com.example.covimap.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.User;
import com.example.covimap.utils.Validator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private EditText editTextPassword;

    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutPassword;

    private AppStatus appStatus;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mappingUIComponent();
        receiveAppStatus();
        subscribeEventButtons();
    }

    private void mappingUIComponent() {
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutPhoneNumber = findViewById(R.id.textInputPhoneNumber);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        mappingRegisterTextView();
    }

    private void mappingRegisterTextView() {
        TextView textViewRegister = findViewById(R.id.textViewCreateAccount);
        textViewRegister.setText(Html.fromHtml("<font color='#000000'>New to app yet. </font>" +
                "<font color='#0c0099'>Create one</font>", Html.FROM_HTML_MODE_LEGACY));

        textViewRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void receiveAppStatus() {
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone-number");
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");

        if (phone != null && !phone.isEmpty()){
            editTextPhoneNumber.setText(phone);
        }
    }

    private void subscribeEventButtons() {
        buttonLogin.setOnClickListener(this::onClick);
    }

    private boolean validate() {
        String password = editTextPassword.getText().toString();

        if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return true;
    }

    private void onClick(View view) {
        String phoneNumber = editTextPhoneNumber.getText().toString();

        if (!Validator.isPhoneNumber(phoneNumber)) {
            textInputLayoutPhoneNumber.setError("Please enter valid phone number");
            return;
        } else {
            textInputLayoutPhoneNumber.setError(null);
        }

        handleLoginAction(phoneNumber);
    }

    private void handleLoginAction(String phoneNumber) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    textInputLayoutPhoneNumber.setError("Your account is not exist!");
                    return;
                }

                login(snapshot);
            }

            private void login(@NonNull DataSnapshot snapshot) {
                if (!validate()) {
                    return;
                }

                User user = snapshot.getValue(User.class);
                String password = editTextPassword.getText().toString();

                String hashedPassword = Hashing.sha256()
                        .hashString(password, StandardCharsets.UTF_8)
                        .toString();

                if (!user.matchPassword(hashedPassword)) {
                    textInputLayoutPassword.setError("Wrong password!");
                    return;
                } else {
                    textInputLayoutPassword.setError(null);
                }

                setLoggedStatus();
                sendingLoggedStatusToMain(user, appStatus);
                logStatus(user, appStatus);

                finish();
            }

            private void setLoggedStatus() {
                appStatus.setLogged(true);
                appStatus.setPhoneNumber(phoneNumber);
            }

            private void sendingLoggedStatusToMain(User user, AppStatus appStatus) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("AccountData", user);
                intent.putExtra("AppStatus", appStatus);
                startActivity(intent);
            }

            private void logStatus(User user, AppStatus appStatus) {
                Log.d("MyLog", user.toString());
                Log.d("MyLog", appStatus.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}