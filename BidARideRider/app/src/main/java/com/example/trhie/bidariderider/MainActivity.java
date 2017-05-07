package com.example.trhie.bidariderider;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void signin_page(View view) {
        Intent it = new Intent(this,SigninActivity.class);
        startActivity(it);
    }

    public void signup_page(View view) {
        Intent it = new Intent(this,SignupActivity.class);
        startActivity(it);
    }
}
