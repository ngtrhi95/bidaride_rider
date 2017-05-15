package com.example.trhie.bidariderider;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import Modules.BookingInfo;
import Modules.DirectionInfo;

/**
 * Created by trhie on 5/13/2017.
 */

public class StatusActivity extends AppCompatActivity {
    BookingInfo bookingInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String now = DateFormat.getDateTimeInstance().format(new Date());
        setTitle(now);
        setContentView(R.layout.activity_status);

        Intent i = getIntent();
        bookingInfo = (BookingInfo) i.getParcelableExtra("bookingInfo");

        TextView tvHeader = (TextView)findViewById(R.id.tvheader);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/28 Days Later.ttf");
        tvHeader.setTypeface(custom_font);

        ((TextView)findViewById(R.id.tvStatusDriverName)).setText(bookingInfo.getDriverName());
        ((TextView)findViewById(R.id.tvStatusDriverPhone)).setText(bookingInfo.getDriverPhone());
        ((TextView)findViewById(R.id.tvfrom)).setText(bookingInfo.getFromAddress());
        ((TextView)findViewById(R.id.tvto)).setText(bookingInfo.getToAddress());
        ((TextView)findViewById(R.id.tvStatusCost)).setText(bookingInfo.getCost());

        ReceiveDirectLog tripTask = new ReceiveDirectLog();
        tripTask.execute(driverID);
    }

    private class ReceiveDirectLog {
    }
}
