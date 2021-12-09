package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CovidPassportActivity extends Activity {
    private FloatingActionButton closeBtn;
    private WebView webView;
    private Button submitButton;
    private User user;

    int countMess = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.covi_passport);

        mappingUIComponent();
        receiveUser();
        configureWebView();
        subscribeEventButton();
    }

    private void mappingUIComponent() {
        webView = findViewById(R.id.web_thong_tin_tiem_chung);
        closeBtn = findViewById(R.id.close_render_history_item);
        submitButton = findViewById(R.id.submit_button);
    }

    private void receiveUser() {
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("MyAccount");
    }

    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebCallBack());
        webView.setWebChromeClient(new WebCommunication());

        String webUrl = "https://tiemchungcovid19.gov.vn/portal/search";
        webView.loadUrl(webUrl);
    }

    private void subscribeEventButton() {
        closeBtn.setOnClickListener(view -> finish());

        submitButton.setOnClickListener(view -> {
            String jsCode = "elem = document.querySelector(\".bgcovid\");\n" +
                    "style = getComputedStyle(elem);\n" +
                    "console.log(style.backgroundColor);";
            webView.evaluateJavascript(jsCode, null);
        });
    }

    private class WebCallBack extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            String jsCode = formJS();
            view.evaluateJavascript(jsCode ,null);
        }

        private String formJS() {
            String name = user.getFullName() + "@";
            String birthday = user.getBirthday() + "@";
            String phone = user.getPhoneNumber()+"@";

            return ("document.querySelector(\"input[formcontrolname='fullname']\").value = \""+ name +"\";\n" +
                    "document.querySelector(\"input[formcontrolname='birthday']\").value = \""+ birthday +"\";\n" +
                    "document.querySelector(\"input[formcontrolname='personalPhoneNumber']\").value = \""+ phone +"\";" +
                    "document.querySelector(\"button[type='submit'\").disabled = false;");
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }
    }

    private class WebCommunication extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String mess = consoleMessage.message();

            if (mess.contains("rgb")) {
                saveColorVaccine(mess);
                countMess++;
            }

            if (countMess == 1) {
                finish();
            }

            return super.onConsoleMessage(consoleMessage);
        }

        private void saveColorVaccine(String mess) {
            String color = convertRGBtoHex(mess);

            SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ColorVaccine", color);
            editor.apply();
        }

        public String convertRGBtoHex(String src){
            src = src.replace("rgb", "")
                    .replace("(", "")
                    .replace(")", "");
            String[] rgb = src.split(", ");

            int r = Integer.parseInt(rgb[0]);
            int g = Integer.parseInt(rgb[1]);
            int b = Integer.parseInt(rgb[2]);

            return String.format("#%02x%02x%02x", r, g, b);
        }
    }
}
