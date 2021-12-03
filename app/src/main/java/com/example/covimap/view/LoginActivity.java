package com.example.covimap.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.Facility;
import com.example.covimap.model.MyAccount;
import com.example.covimap.model.Route;
import com.example.covimap.model.User;
import com.example.covimap.repository.FacilityRepository;
import com.example.covimap.repository.RedzoneRepository;
import com.example.covimap.repository.RouteRepository;
import com.example.covimap.utils.SQLiteHelper;
import com.example.covimap.utils.Validator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private EditText editTextPassword;

    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutPassword;

    private String phoneNumber, password;
    private AppStatus appStatus;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initEventButton();
        //testRepository();
    }

    private void initViews() {
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutPhoneNumber = findViewById(R.id.textInputPhoneNumber);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone-number");
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");
        if(phone != null && !phone.isEmpty()){
            editTextPhoneNumber.setText(phone);
        }
        initRegisterTextView();
    }

    private void initRegisterTextView() {
        TextView textViewRegister = findViewById(R.id.textViewCreateAccount);
        textViewRegister.setText(Html.fromHtml("<font color='#000000'>New to app yet. </font>" +
                "<font color='#0c0099'>Create one</font>", Html.FROM_HTML_MODE_LEGACY));
        textViewRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void initEventButton() {
        buttonLogin.setOnClickListener(view -> {
            phoneNumber = editTextPhoneNumber.getText().toString();
            if (!Validator.isPhoneNumber(phoneNumber)) {
                textInputLayoutPhoneNumber.setError("Please enter valid phone number");
                return;
            }
            else{
                textInputLayoutPhoneNumber.setError(null);
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber);
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            MyAccount myAccount = snapshot.getValue(MyAccount.class);
                            if(validate()){
                                if(myAccount.getPassword().equals(password)) {
                                    textInputLayoutPassword.setError(null);
                                    appStatus.setLogged(true);
                                    appStatus.setPhoneNumber(phoneNumber);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("AccountData", myAccount);
                                    intent.putExtra("AppStatus", appStatus);
                                    Log.d("MyLog", myAccount.toString());
                                    Log.d("MyLog", appStatus.toString());
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    textInputLayoutPassword.setError("Wrong password!");
                                }
                            }else {return;}
                        }
                        else {
                            textInputLayoutPhoneNumber.setError("Your account is not exist!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private boolean validate() {
        password = editTextPassword.getText().toString();
        if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        }else {textInputLayoutPassword.setError(null);}

        return true;
    }
}