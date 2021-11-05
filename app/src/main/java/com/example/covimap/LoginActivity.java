package com.example.covimap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.model.CLocation;
import com.example.covimap.model.Facility;
import com.example.covimap.model.Route;
import com.example.covimap.model.User;
import com.example.covimap.repository.FacilityRepository;
import com.example.covimap.repository.RedzoneRepository;
import com.example.covimap.repository.RouteRepository;
import com.example.covimap.utils.SQLiteHelper;
import com.example.covimap.utils.Validator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail;
    EditText editTextPassword;
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;
    Button buttonLogin;
    SQLiteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initEventButton();

        sqliteHelper = new SQLiteHelper(this);

        //testRepository();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        initRegisterTextView();
    }

    private void initRegisterTextView() {
        TextView textViewRegister = findViewById(R.id.textViewCreateAccount);
        textViewRegister.setText(Html.fromHtml("<font color='#000000'>Don't have account yet. </font>" +
                "<font color='#0c0099'>create one</font>", Html.FROM_HTML_MODE_LEGACY));
        textViewRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void initEventButton() {
        buttonLogin.setOnClickListener(view -> {
            if (!validate()) {
                return;
            }

            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            User authenticatedUser = sqliteHelper.authenticate(new User(null, null, email, password));

            if (authenticatedUser != null) {
                Snackbar.make(buttonLogin, "Successfully Logged in!", Snackbar.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Snackbar.make(buttonLogin, "Failed to log in , please try again", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public boolean validate() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (!Validator.isEmail(email)) {
            textInputLayoutEmail.setError("Please enter valid email!");
            return false;
        } else if (!Validator.isPassword(password)) {
            textInputLayoutPassword.setError("Please enter valid password!");
            return false;
        } else {
            textInputLayoutEmail.setError(null);
            textInputLayoutPassword.setError(null);
        }

        return true;
    }

    private void testRepository() {
        Facility facility = new Facility(new CLocation(1, 2), "a", "b", "c");
        FacilityRepository facilityRepository = new FacilityRepository();
        facilityRepository.add(facility);
        System.out.println(facility.getUuid());

        List<CLocation> path = Arrays.asList(new CLocation(1, 2));
        Route route = new Route(path, "123", "12-01 04:02:01", 123);
        RouteRepository routeRepository = new RouteRepository();
        routeRepository.add(route);
        System.out.println(route.getUuid());

        RedzoneRepository redzoneRepository = new RedzoneRepository();
        CLocation redzone = new CLocation(1, 2);
        redzoneRepository.add(redzone);
        System.out.println(redzone.getUuid());
    }
}