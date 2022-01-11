package com.example.covimap.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputLayout textInputOldPassword;
    private EditText editTextOldPassword;

    private TextInputLayout textInputNewPassword;
    private EditText editTextNewPassword;

    private TextInputLayout textInputConfirmPassword;
    private EditText editTextConfirmPassword;

    private Button buttonSubmit;

    private AppStatus appStatus;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mappingUIComponent();
        receiveAppStatus();
        subscribeEventButtons();
    }

    private void mappingUIComponent() {
        textInputOldPassword = findViewById(R.id.textInputOldPassword);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        textInputNewPassword = findViewById(R.id.textInputNewPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSubmit = findViewById(R.id.buttonSubmit);
    }

    private void receiveAppStatus() {
        Intent intent = getIntent();
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");
        user = (User)intent.getSerializableExtra("AccountData");
    }

    private void subscribeEventButtons() {
        buttonSubmit.setOnClickListener(this::onClick);
    }

    private boolean validate() {
        String oldPassword = editTextOldPassword.getText().toString();
        if (!Validator.isPassword(oldPassword)) {
            textInputOldPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputOldPassword.setError(null);
        }

        String newPassword = editTextNewPassword.getText().toString();
        if (!Validator.isPassword(newPassword)) {
            textInputNewPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputNewPassword.setError(null);
        }

        String confirmPassword = editTextConfirmPassword.getText().toString();
        if (!Validator.isPassword(confirmPassword)) {
            textInputConfirmPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputConfirmPassword.setError(null);
        }

        return true;
    }

    private void onClick(View view) {
        if (!validate()) {
            return;
        }

        String phoneNumber = user.getPhoneNumber();
        handleResetPassword(phoneNumber);
    }

    private void handleResetPassword(String phoneNumber) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user == null) {
                    return;
                }

                String oldPassword = editTextOldPassword.getText().toString();
                String hashedOldPassword = Hashing.sha256()
                        .hashString(oldPassword, StandardCharsets.UTF_8)
                        .toString();
                if (!user.matchPassword(hashedOldPassword)) {
                    textInputOldPassword.setError("Incorrect password!");
                    return;
                } else {
                    textInputOldPassword.setError(null);
                }

                String newPassword = editTextNewPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();
                if (!newPassword.equals(confirmPassword)) {
                    textInputConfirmPassword.setError("Confirm password not matches!");
                    return;
                } else {
                    textInputConfirmPassword.setError(null);
                }

                String hashedPassword = Hashing.sha256()
                        .hashString(newPassword, StandardCharsets.UTF_8)
                        .toString();
                user.setPassword(hashedPassword);
                mDatabase.setValue(user);

                Toast.makeText(ResetPasswordActivity.this,
                        "Reset password successfully. Please login again!", Toast.LENGTH_SHORT).show();

                Intent loginAgain = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                loginAgain.putExtra("AccountData", user);
                loginAgain.putExtra("AppStatus", appStatus);
                startActivity(loginAgain);

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
