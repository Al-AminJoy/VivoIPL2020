package com.trustedoffers.vivoipl2020;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goHome();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
            }
        }, SPLASH_TIME_OUT);
    }

    public void goHome(){
        Intent home = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(home);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        //finish();
    }

    @Override
    public void onBackPressed() {

    }
}
