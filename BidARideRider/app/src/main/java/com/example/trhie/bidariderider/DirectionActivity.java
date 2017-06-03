package com.example.trhie.bidariderider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;

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

import static com.example.trhie.bidariderider.UserSession.KEY_FNAME;
import static com.example.trhie.bidariderider.UserSession.KEY_ID;
import static com.example.trhie.bidariderider.UserSession.KEY_PHONE;
import static com.example.trhie.bidariderider.UserSession.PREFER_NAME;

/**
 * Created by trhie on 4/30/2017.
 */

public class DirectionActivity extends AppCompatActivity implements DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {
    public static android.content.SharedPreferences SharedPreferences = null;
    EditText etSrcAddress, etDesAddress;
    TextView tvFullname, tvPhone;
    static PlaceInfo originPlace = new PlaceInfo();
    static PlaceInfo desPlace = new PlaceInfo();
    DirectionInfo directionInfo = new DirectionInfo();
    ProgressDialog progressDialog;
    UserSession session;
    String costStr;
    int cost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        tvFullname = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userfullname);
        tvPhone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userphone);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        etSrcAddress = (EditText)findViewById(R.id.srcpst);
        etDesAddress = (EditText)findViewById(R.id.despst);

        session = new UserSession(getApplicationContext());

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);


        session.checkLogin();

        String userID = SharedPreferences.getString(KEY_ID, "");
        String s = SharedPreferences.getString(KEY_FNAME, "");
        tvFullname.setText(SharedPreferences.getString(KEY_FNAME, ""));
        tvPhone.setText(SharedPreferences.getString(KEY_PHONE, ""));

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            cost = distance * 3;
            costStr = Integer.toString(cost) + " VNĐ";
            ((TextView) findViewById(R.id.tvCost)).setText("Cost: " + costStr);
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

        id = SharedPreferences.getString(KEY_ID, "");
        directionInfo.setUserID(id);

        directionInfo.setOriginInfo(originPlace);
        directionInfo.setDesAddress(desPlace.getAddress());
        directionInfo.setDesLocation(desPlace.getLocation());
        directionInfo.setCost(cost);
        i.putExtra("originPlace", directionInfo);
        startActivity(i);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
// Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            session.logoutUser();
            startActivity(new Intent(DirectionActivity.this, MainActivity.class));
        } if (id == R.id.nav_history){
            startActivity(new Intent(DirectionActivity.this, LogsActivity.class));

        } else if (id == R.id.nav_aboutus) {
            startActivity(new Intent(DirectionActivity.this, AboutusActivity.class));
        } else if (id == R.id.nav_promotion){
            startActivity(new Intent(DirectionActivity.this, PromotionActivity.class));
        } else if (id == R.id.nav_share) {
            startActivity(new Intent(DirectionActivity.this, ShareActivity.class));
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(DirectionActivity.this, SupportActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
