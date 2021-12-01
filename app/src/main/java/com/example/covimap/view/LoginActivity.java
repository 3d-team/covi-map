package com.example.covimap.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.R;
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

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private EditText editTextPassword;

    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutPassword;

    private String phoneNumber, password;

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
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
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

    private void testRepository() {
        Facility facility = new Facility(new CLocation(1, 2), "a", "b", "c");
        FacilityRepository facilityRepository = new FacilityRepository();
        facilityRepository.add(facility);
        System.out.println(facility.getUuid());

        List<CLocation> path = Arrays.asList(new CLocation(1, 2));
        Route route = new Route(path, "123", "77749" ,"12-01 04:02:01", 123);
        RouteRepository routeRepository = new RouteRepository();
        routeRepository.add(route);
        System.out.println(route.getUuid());

        RedzoneRepository redzoneRepository = new RedzoneRepository();
        CLocation redzone = new CLocation(1, 2);
        redzoneRepository.add(redzone);
        System.out.println(redzone.getUuid());
    }
}