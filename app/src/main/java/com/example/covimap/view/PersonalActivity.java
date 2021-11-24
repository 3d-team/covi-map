package com.example.covimap.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.config.LanguageConfig;
import com.example.covimap.model.AppStatus;
import com.example.covimap.service.PersonalFragmentCallbacks;

import java.util.Locale;

public class PersonalActivity extends Fragment implements PersonalFragmentCallbacks {
    private MainActivity main;
    private Context context;
    private static View view;
    private static AppStatus createStatus;

    private RadioGroup languageOptionRG;
    private RadioButton vi_button;
    private RadioButton en_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.personal_activity, null);

        languageOptionRG = view.findViewById(R.id.language_option);
        vi_button = view.findViewById(R.id.vietnamese_radiobutton);
        en_button = view.findViewById(R.id.english_radiobutton);

        languageOptionRG.clearCheck();
        if(createStatus.getLanguage().equals(LanguageConfig.VI)){
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

    private View.OnClickListener vi_onclick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new AlertDialog.Builder(context).setMessage(R.string.mess_change_language)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        main.onChangeLanguage(LanguageConfig.VI);
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
                            main.onChangeLanguage(LanguageConfig.EN);
                        }
                    })
                    .show();
        }
    };

    @Override
    public void setStatus(AppStatus appStatus){
        this.createStatus = appStatus;
    }
}
