package com.example.trhie.bidariderider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by trhie on 5/12/2017.
 */

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
    }

    public void sendFeedBack(View view) {

    }

    public void callPhone(View view) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "+841685501371", null));
        //callIntent.setData(Uri.parse("tel:+841672172119"));
        startActivity(callIntent);
    }
}
