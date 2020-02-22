package com.trustedoffers.vivoipl2020;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DeeplinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deeplink);

        Intent brint = getIntent();
        String data = getIntent().getDataString();

        Intent home = new Intent(getApplication(), MainActivity.class);
        home.putExtra("burl",data);
        home.setAction("deeplink");
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(home);
        finish();
        // browser.loadUrl(data);

    }
}
