package com.example.trhie.bidariderider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.DirectionInfo;
import Modules.PlaceInfo;
import Modules.Route;

/**
 * Created by trhie on 4/30/2017.
 */

public class DirectionActivity extends AppCompatActivity implements DirectionFinderListener {
    EditText etSrcAddress, etDesAddress;
    static PlaceInfo originPlace = new PlaceInfo();
    static PlaceInfo desPlace = new PlaceInfo();
    DirectionInfo directionInfo = new DirectionInfo();
    static JSONObject userObject;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        etSrcAddress = (EditText)findViewById(R.id.srcpst);
        etDesAddress = (EditText)findViewById(R.id.despst);

        if (userObject == null) {
            try {
                userObject = new JSONObject(getIntent().getStringExtra("userObject"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent i = getIntent();
        PlaceInfo lastLocation = (PlaceInfo) i.getParcelableExtra("lastLocation");
        if (lastLocation != null) {
            if (lastLocation.getIdMap() == 1) {
                originPlace = lastLocation;
            }
            else if (lastLocation.getIdMap() == 2) {
                desPlace = lastLocation;
            }
        }

        if (originPlace.getAddress() != null) {
            etSrcAddress.setText(originPlace.getAddress());
        }
        if (desPlace.getAddress() != null) {
            etDesAddress.setText(desPlace.getAddress());
        }

        if (originPlace.getLocation() != null && desPlace.getLocation() != null){
            try {
                DirectionFinder df = new DirectionFinder(this, originPlace.getLocation(), desPlace.getLocation());
                df.execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void showSrcMap(View view) {
        Intent it = new Intent(this, MapsActivity.class);
        it.putExtra("SrcMap", 1);
        startActivity(it);
    }

    public void showDesMap(View view) {
        Intent it = new Intent(this, MapsActivity.class);
        it.putExtra("DesMap", 2);
        startActivity(it);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Calculating direction..!", true);
    }

    List<Route> lstRoutes;
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        lstRoutes = routes;
        progressDialog.dismiss();
        for (Route route : routes) {
            ((TextView) findViewById(R.id.tvDirection)).setText("Distance: " + route.distance.text);
            int distance = route.distance.value;
            int cost = distance * 3;
            ((TextView) findViewById(R.id.tvCost)).setText("Cost: " + Integer.toString(cost) + " VNĐ");
        }

    }

    public void showDirection(View view) {
        if (desPlace.getAddress()==null|| originPlace.getAddress()==null){
            Toast.makeText(DirectionActivity.this, "Please fill in source and destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MapdirectionActivity.class);
        intent.putParcelableArrayListExtra("lstRoutes", (ArrayList<? extends Parcelable>) lstRoutes);
        startActivity(intent);
    }

    public void findDriver(View view) {
        if (desPlace.getAddress()==null|| originPlace.getAddress()==null){
            Toast.makeText(DirectionActivity.this, "Please fill in source and destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(DirectionActivity.this, ListDriver.class);
        String id = null;
        try {
            id = userObject.get("userID").toString();
            directionInfo.setUserID(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        directionInfo.setOriginInfo(originPlace);
        directionInfo.setDesAddress(desPlace.getAddress());
        directionInfo.setDesLocation(desPlace.getLocation());
        i.putExtra("originPlace", directionInfo);
        startActivity(i);
    }
}
