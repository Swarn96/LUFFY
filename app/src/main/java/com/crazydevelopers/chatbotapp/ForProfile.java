package com.crazydevelopers.chatbotapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ForProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_profile);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
