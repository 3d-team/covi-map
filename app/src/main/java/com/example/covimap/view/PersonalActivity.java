package com.example.covimap.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class PersonalActivity extends Fragment implements PersonalFragmentCallbacks {
    private MainActivity main;
    private Context context;
    private static View view;
    private static AppStatus createStatus;
    private User user;

    private RadioGroup languageOptionRG;
    private RadioButton vi_button;
    private RadioButton en_button;
    private TextView fullNameTextView;
    private TextView birthdayTextView;
    private TextView logoutButton;
    private TextView updateButton;

    private LinearLayout passportLayout;
    private ImageView qrCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.personal_activity, null);

        languageOptionRG = view.findViewById(R.id.language_option);
        vi_button = (RadioButton) view.findViewById(R.id.vietnamese_radiobutton);
        en_button = (RadioButton) view.findViewById(R.id.english_radiobutton);
        fullNameTextView = (TextView) view.findViewById(R.id.fullname_textview);
        birthdayTextView = (TextView) view.findViewById(R.id.birthday_textview);

        fullNameTextView.setText(user.getFullName());
        birthdayTextView.setText(user.getBirthday());

        logoutButton = (TextView)view.findViewById(R.id.logout_textview_button);
        logoutButton.setOnClickListener(logoutAction);
        updateButton = (TextView) view.findViewById(R.id.update_passport_text_view);
        updateButton.setOnClickListener(updateAction);

        passportLayout = (LinearLayout) view.findViewById(R.id.covid_passport);
        qrCode = (ImageView) view.findViewById(R.id.qr_code_imgview);

        languageOptionRG.clearCheck();
        if(createStatus.getLanguage().equals(Language.VI)){
            vi_button.setChecked(true);
        }
        else {
            en_button.setChecked(true);
        }

        vi_button.setOnClickListener(vi_onclick);
        en_button.setOnClickListener(en_onclick);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            context = getActivity();
            main = (MainActivity) getActivity();
        }
        catch (IllegalStateException e){
            throw new IllegalStateException("Error");
        }
    }

    private View.OnClickListener logoutAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(main, PrepareActivity.class);
            main.onChangeLoginStatus(false);
//            intent.putExtra("phone-number", myAccount.getPhoneNumber());
//            intent.putExtra("AppStatus", createStatus);
            startActivity(intent);
        }
    };


    private View.OnClickListener vi_onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(context).setMessage(R.string.mess_change_language)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        main.onChangeLanguage(String.valueOf(Language.VI));
                    }
                })
                .show();
        }
    };

    private View.OnClickListener en_onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(context).setMessage(R.string.mess_change_language)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            main.onChangeLanguage(String.valueOf(Language.EN));
                        }
                    })
                    .show();
        }
    };

    private View.OnClickListener updateAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, CovidPassportActivity.class);
            intent.putExtra("MyAccount", user);
            startActivity(intent);
        }
    };

    @Override
    public void setStatus(AppStatus appStatus){
        this.createStatus = appStatus;
    }

    @Override
    public void setMyAccount(User user) {
        this.user = user;
    }

    @Override
    public void setUpCovidPassPort(String color, Bitmap qrCode) {

    }

    @Override
    public void onResume() {
        super.onResume();
        // setup Color
        String value = createStatus.getColor();
        passportLayout.setBackgroundColor(Color.parseColor(value));
        SharedPreferences preferences = main.getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
        String color = "#"+Config.GRAY_ZONE_COLOR;
        String key = "ColorVaccine";
        if(preferences != null && preferences.contains(key)){
            color = preferences.getString(key, "");
            passportLayout.setBackgroundColor(Color.parseColor(color));
            main.onColorChange(color);
        }

//        value = createStatus.getQRCode();
//        key = "QR_Code";
//        if(preferences != null && preferences.contains(key)){
//            value = preferences.getString(key, "");
//            main.onQRCodeChange(value);
//        }
        value = "https://chart.googleapis.com/chart?cht=qr&chl=CoviMap_Project_of_477_484_495&chs=180x180&choe=UTF-8&chld=L|2";
        new DownloadImage().execute(value);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            qrCode.setImageBitmap(result);
            // Close progressdialog
        }
    }
}
