package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.MyAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CoviPassportAcivity extends Activity{
    private FloatingActionButton closeBtn;
    private WebView webView;
    private Button submitButton;
    private String color;
    private Bitmap qrCode;
    private MyAccount myAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.covi_passport);
        webView = findViewById(R.id.web_thong_tin_tiem_chung);

        Intent intent = getIntent();
        myAccount = (MyAccount) intent.getSerializableExtra("MyAccount");

        WebSettings webSettings =  webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebCallBack());
        webView.setWebChromeClient(new WebComunication());
        webView.loadUrl("https://tiemchungcovid19.gov.vn/portal/search");

        closeBtn = (FloatingActionButton) findViewById(R.id.close_render_history_item);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jsCode = "var qr_element = document.querySelector(\"div.qrcode img\");\n" +
                        "console.log(qr_element.src);";
                webView.evaluateJavascript(jsCode, null);
                jsCode = "elem = document.querySelector(\".bgcovid\");\n" +
                        "style = getComputedStyle(elem);\n" +
                        "console.log(style.backgroundColor);";
                webView.evaluateJavascript(jsCode, null);
            }
        });
    }

    private class WebCallBack extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            String name = myAccount.getFullname() + "@";
//            String birthday = myAccount.getBirthday() + "@";
//            String phone = myAccount.getPhoneNumber()+"@";
            Log.d("MyLog_WEBLOG", myAccount.toString());
            String name = "Trầm Hữu Đức"+"@";
            String birthday = "01/09/2001"+"@";
            String phone = "0375157298"+"@";

            String jsCode = "var qr_element = document.querySelector(\"div.qrcode img\");\n" +
                    "console.log(qr_element.src);";
            view.evaluateJavascript(jsCode, null);

            jsCode = "document.querySelector(\"input[formcontrolname='fullname']\").value = \""+ name +"\";\n" +
                    "document.querySelector(\"input[formcontrolname='birthday']\").value = \""+ birthday +"\";\n" +
                    "document.querySelector(\"input[formcontrolname='personalPhoneNumber']\").value = \""+ phone +"\";" +
                    "document.querySelector(\"button[type='submit'\").disabled = false;";
            view.evaluateJavascript(jsCode ,null);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }
    }

    int countMess = 0;
    private class WebComunication extends WebChromeClient{
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String mess = consoleMessage.message();

            if(mess.contains("rgb")){
                Log.d("MyTag", consoleMessage.message());
                color = convertRGBtoHex(mess);
                SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ColorVaccine", color);
                editor.commit();
                countMess++;
            }
            if(mess.contains("data")){
                Log.d("MyTag", consoleMessage.message());
                SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("QR_Code", mess);
                editor.commit();
                countMess++;
            }
            if(countMess == 2){
                finish();
            }
            return super.onConsoleMessage(consoleMessage);
        }
    }

    public String convertRGBtoHex(String src){
        src = src.replace("rgb", "");
        src = src.replace("(", "");
        src = src.replace(")", "");
        String rgb[] = src.split(", ");
        int r = Integer.parseInt(rgb[0]);
        int g = Integer.parseInt(rgb[1]);
        int b = Integer.parseInt(rgb[2]);
        String hex = String.format("#%02x%02x%02x", r, g, b);
        Log.d("COLOR", hex);
        return hex;
    }

    //link: https://stackoverflow.com/questions/18210700/best-method-to-download-image-from-url-in-android
    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
