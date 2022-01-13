package com.example.covimap.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.Language;
import com.example.covimap.model.User;
import com.example.covimap.service.PersonalFragmentCallbacks;

import java.io.InputStream;
import java.net.URL;

public class PersonalFragment extends Fragment implements PersonalFragmentCallbacks {
    private MainActivity main;
    private Context context;
    private View view;
    private static AppStatus createStatus;
    private User user;

    private RadioGroup languageOptionRG;
    private RadioButton VILanguageButton;
    private RadioButton ENLanguageButton;
    private TextView fullNameTextView;
    private TextView birthdayTextView;
    private TextView logoutButton;
    private TextView updateButton;
    private TextView resetPasswordButton;
    private LinearLayout passportLayout;
    private ImageView qrCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.personal_activity, null);

        mappingUIComponent();
        subscribeEventButton();
        initializeComponent();

        return view;
    }

    private void mappingUIComponent() {
        languageOptionRG = view.findViewById(R.id.language_option);
        VILanguageButton = view.findViewById(R.id.vietnamese_radiobutton);
        ENLanguageButton = view.findViewById(R.id.english_radiobutton);
        fullNameTextView = view.findViewById(R.id.fullname_textview);
        birthdayTextView = view.findViewById(R.id.birthday_textview);
        logoutButton = view.findViewById(R.id.logout_textview_button);
        updateButton = view.findViewById(R.id.update_passport_text_view);
        passportLayout = view.findViewById(R.id.covid_passport);
        resetPasswordButton = view.findViewById(R.id.reset_password_text_view);
        qrCode = view.findViewById(R.id.qr_code_imgview);
    }

    private void subscribeEventButton() {
        logoutButton.setOnClickListener(view -> {
            Intent intent = new Intent(main, PrepareActivity.class);
            main.onChangeLoginStatus(false);
            startActivity(intent);
        });

        updateButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, CovidPassportActivity.class);
            intent.putExtra("MyAccount", user);
            startActivity(intent);
        });

        VILanguageButton.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setMessage(R.string.mess_change_language)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> main.onChangeLanguage(Language.VI))
                .show());

        ENLanguageButton.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setMessage(R.string.mess_change_language)
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> main.onChangeLanguage(Language.EN))
                .show());

        resetPasswordButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
            intent.putExtra("AppStatus", createStatus);
            intent.putExtra("AccountData", user);
            startActivity(intent);
        });
    }

    private void initializeComponent() {
        fullNameTextView.setText(user.getFullName());
        birthdayTextView.setText(user.getBirthday());

        languageOptionRG.clearCheck();

        if (createStatus.getLanguage().equals(Language.VI)) {
            ENLanguageButton.setChecked(false);
            VILanguageButton.setChecked(true);
        } else {
            ENLanguageButton.setChecked(true);
            VILanguageButton.setChecked(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Error");
        }
    }

    @Override
    public void setStatus(AppStatus appStatus) {
        createStatus = appStatus;
    }

    @Override
    public void setMyAccount(User user) {
        this.user = user;
    }

    @Override
    public void onResume() {
        super.onResume();

        changeBgColor();
        downloadQRCode();
    }

    private void changeBgColor() {
        changeBgColorFollowStatus();
        changeBgColorFollowVaccineColor();
    }

    private void changeBgColorFollowStatus() {
        String bgColor = createStatus.getColor();
        passportLayout.setBackgroundColor(Color.parseColor(bgColor));
    }

    private void changeBgColorFollowVaccineColor() {
        String key = "ColorVaccine";
        SharedPreferences preferences = main.getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
        if (preferences == null || !preferences.contains(key)) {
            return;
        }

        String color = preferences.getString(key, "");
        passportLayout.setBackgroundColor(Color.parseColor(color));
        main.onColorChange(color);
    }

    private void downloadQRCode() {
        String url = "https://chart.googleapis.com/chart?cht=qr&chl=CoviMap_Project_of_477_484_495&chs=180x180&choe=UTF-8&chld=L|2";
        DownloadImage downloadImageTask = new DownloadImage();
        downloadImageTask.execute(url);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String imageURL = url[0];
            Bitmap bitmap = null;

            try (InputStream input = new URL(imageURL).openStream()) {
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            qrCode.setImageBitmap(result);
        }
    }
}
