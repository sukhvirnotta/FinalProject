/*
SplashActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Set up activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // define layout for activity's user interface
        setContentView(R.layout.activity_splash);

        int SPLASH_TIMEOUT = 1000;
        new Handler().postDelayed(new Runnable() {
            // Go to login page after timeout
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}
