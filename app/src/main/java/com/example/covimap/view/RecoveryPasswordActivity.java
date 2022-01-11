package com.example.covimap.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.User;
import com.example.covimap.service.MailSenderService;
import com.example.covimap.utils.PasswordGenerator;
import com.example.covimap.utils.Validator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.hash.Hashing;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;

public class RecoveryPasswordActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private TextInputLayout textInputLayoutPhoneNumber;
    private Button buttonLogin;

    private AppStatus appStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_password);

        mappingUIComponent();
        receiveAppStatus();
        subscribeEventButtons();
    }

    private void mappingUIComponent() {
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        textInputLayoutPhoneNumber = findViewById(R.id.textInputPhoneNumber);
        buttonLogin = findViewById(R.id.buttonLogin);

        mappingRegisterTextView();
    }

    private void mappingRegisterTextView() {
        TextView textViewRegister = findViewById(R.id.textViewCreateAccount);
        textViewRegister.setText(Html.fromHtml("<font color='#000000'>New to app yet. </font>" +
                "<font color='#0c0099'>Create one</font>", Html.FROM_HTML_MODE_LEGACY));

        textViewRegister.setOnClickListener(view -> {
            Intent intent = new Intent(RecoveryPasswordActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void receiveAppStatus() {
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone-number");
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");

        if (phone == null || phone.isEmpty()) {
            return;
        }
        editTextPhoneNumber.setText(phone);
    }

    private void subscribeEventButtons() {
        buttonLogin.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        String phoneNumber = editTextPhoneNumber.getText().toString();

        if (!Validator.isPhoneNumber(phoneNumber)) {
            textInputLayoutPhoneNumber.setError("Please enter valid phone number");
            return;
        }
        textInputLayoutPhoneNumber.setError(null);

        handleRecoveryPassword(phoneNumber);
    }

    private void handleRecoveryPassword(String phoneNumber) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    textInputLayoutPhoneNumber.setError("Phone number doesn't exists!");
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user == null) {
                    textInputLayoutPhoneNumber.setError("Account doesn't exists!");
                    return;
                }

                String newPassword = generateSecurePassword();
                resetPasswordToFirebase(user, newPassword);
                sendResetPasswordToUser(newPassword);
                triggerLoginAgain(user);

                System.out.println(newPassword);
                finish();
            }

            private String generateSecurePassword() {
                PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                        .useDigits(true)
                        .useLower(true)
                        .useUpper(true)
                        .build();
                return passwordGenerator.generate(8);
            }

            private void resetPasswordToFirebase(User user, String password) {
                String hashedPassword = Hashing.sha256()
                        .hashString(password, StandardCharsets.UTF_8)
                        .toString();
                user.setPassword(hashedPassword);
                mDatabase.setValue(user);
            }

            private void sendResetPasswordToUser(String password) {
                SendMailTask sendMailTask = new SendMailTask();
                sendMailTask.execute(password);
            }

            private void triggerLoginAgain(User user) {
                Toast.makeText(RecoveryPasswordActivity.this,
                        "Recovery password successfully. Please check your email!", Toast.LENGTH_SHORT).show();

                Intent loginAgain = new Intent(RecoveryPasswordActivity.this, LoginActivity.class);
                loginAgain.putExtra("AccountData", user);
                loginAgain.putExtra("AppStatus", appStatus);
                startActivity(loginAgain);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private class SendMailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            MailSenderService mailSenderService = new MailSenderService();
            mailSenderService.sendMail("hebiitachi0@gmail.com", strings[0]);
            return null;
        }
    }
}

