package com.example.trhie.bidariderider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import Modules.BookingInfo;
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.DirectionInfo;
import Modules.DriverAdapter;
import Modules.PlaceInfo;
import Modules.Route;
import Modules.Trip;

import static com.example.trhie.bidariderider.UserSession.KEY_FNAME;
import static com.example.trhie.bidariderider.UserSession.KEY_ID;
import static com.example.trhie.bidariderider.UserSession.KEY_PHONE;
import static com.example.trhie.bidariderider.UserSession.KEY_TOKEN;
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
    private static String AMQP_URL = "amqp://imtqjgzz:LQWyhmVxKBMgV6ROObew36G07DUs6ZYZ@white-mynah-bird.rmq.cloudamqp.com/imtqjgzz";
    private static String EXCHANGE_NAME_LOCATION = "notification_logs";

    JSONArray driverObject;
    Driver driver;

    int cost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new UserSession(getApplicationContext());

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);


        session.checkLogin();
        if (!session.checkLogin()){
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
            int mod = cost % 1000;
            if (mod <= 500){
                cost -= mod;
            }
            else {
                cost += 1000 - mod;
            }
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
        //Intent i = new Intent(DirectionActivity.this, ListDriver.class);
        String id = null;

        id = SharedPreferences.getString(KEY_ID, "");
        directionInfo.setUserID(id);

        directionInfo.setOriginInfo(originPlace);
        directionInfo.setDesAddress(desPlace.getAddress());
        directionInfo.setDesLocation(desPlace.getLocation());
        directionInfo.setCost(cost);
        NetworkingFindDriverAPI n = new NetworkingFindDriverAPI();
        n.execute("https://appluanvan-apigateway.herokuapp.com/api/driver/coordInfo");

        //i.putExtra("originPlace", directionInfo);
        //startActivity(i);
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
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(DirectionActivity.this, SupportActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void SendInforToDriver (Driver temp){
        final Trip tripInfo = createTripInfo(temp);
        Gson gson = new Gson();
        String json = gson.toJson(tripInfo);

        Toast.makeText(DirectionActivity.this, "Booking success. Driver will contact you in a few minutes.", Toast.LENGTH_SHORT).show();
                                        /*ListDriver.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                NetworkingCreateTrip networking = new NetworkingCreateTrip(getApplicationContext());
                                                networking.execute("https://appluanvan-apigateway.herokuapp.com/api/trip/create", tripInfo);
                                            }
                                        });*/
        EmitTripLogs notiTask = new EmitTripLogs();
        notiTask.execute(json.toString());

        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setDriverName(temp.driverFullname);
        bookingInfo.setFromAddress(directionInfo.getOriginInfo().getAddress());
        bookingInfo.setDriverPhone(temp.driverPhone);
        bookingInfo.setToAddress(directionInfo.getDesAddress());
        bookingInfo.setCost(directionInfo.getCost());
        Intent it = new Intent(DirectionActivity.this, StatusActivity.class);
        it.putExtra("bookingInfo", bookingInfo);
        startActivity(it);
    }

    private Trip createTripInfo(Driver temp) {
        Trip tripInfo = new Trip();
        tripInfo.setUserID(directionInfo.getUserID());
        tripInfo.setDriverID(temp.driverID);
        tripInfo.setTripFrom(directionInfo.getOriginInfo().getAddress());
        tripInfo.setTripTo(directionInfo.getDesAddress());
        tripInfo.setFromLong(directionInfo.getOriginInfo().getLocation().longitude);
        tripInfo.setFromLat(directionInfo.getOriginInfo().getLocation().latitude);
        tripInfo.setToLong(directionInfo.getDesLocation().longitude);
        tripInfo.setToLat(directionInfo.getDesLocation().latitude);
        tripInfo.setPrice(directionInfo.getCost());
        tripInfo.setUsername(SharedPreferences.getString(KEY_FNAME, ""));
        tripInfo.setUserphone(SharedPreferences.getString(KEY_PHONE, ""));
        String now = DateFormat.getDateTimeInstance().format(new Date());
        tripInfo.setCreatedDate(now);
        return tripInfo;
    }

    protected  class Driver {
        String driverID;
        String driverFullname;
        String driverPhone;
        float longitude;
        float latitude;
    }

    public class NetworkingFindDriverAPI extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(DirectionActivity.this, "Please wait.",
                    "Finding driver..!", true);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            getJson((String) params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            AutoChooseDriver();
        }

        private void getJson(String url) {
            BufferedReader in = null;
            String data = null;

            try {
                HttpClient client = new DefaultHttpClient();

                URI website = new URI(url);
                HttpGet request = new HttpGet();
                String token = SharedPreferences.getString(KEY_TOKEN, "");
                request.setHeader("x-access-token", token);
                request.setURI(website);
                HttpResponse response = client.execute(request);
                response.getStatusLine().getStatusCode();

                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String l = "";
                String nl = System.getProperty("line.separator");
                while ((l = in.readLine()) != null) {
                    sb.append(l + nl);
                }
                in.close();
                data = sb.toString();
                decodeResultIntoJson(data);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                        return;
                    } catch (Exception e) {
                        Log.e("GetMethodEx", e.getMessage());
                    }
                }
            }


        }
        private void decodeResultIntoJson(String response) {
            try {
                JSONObject jo = new JSONObject(response);
                int status = jo.getInt("status");
                driverObject = jo.getJSONArray("payload");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private List<Driver> getListDriverNearOrigin () {

            final List<Driver> lstDriverNearYou = new ArrayList<Driver>();

            Location locationA = new Location("origin");
            locationA.setLatitude(directionInfo.getOriginInfo().getLocation().latitude);
            locationA.setLongitude(directionInfo.getOriginInfo().getLocation().longitude);

            Gson gson = new Gson();
            for (int i = 0; i < driverObject.length(); i++) {
                try {
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(driverObject.getString(i));
                    Driver driver = gson.fromJson(mJson, Driver.class);

                    Location locationB = new Location("driver location");
                    locationB.setLatitude(driver.latitude);
                    locationB.setLongitude(driver.longitude);

                    float distance = locationA.distanceTo(locationB);
                    if (distance <= 5000) {
                        lstDriverNearYou.add(driver);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return lstDriverNearYou;
        }
        private void AutoChooseDriver() {
            List<Driver> lstDriver = getListDriverNearOrigin ();
            int n = (int )(Math.random() * lstDriver.size() - 1);
            driver = lstDriver.get(n);
            if (driver != null && !driver.driverFullname.equals("")){
                Toast.makeText(DirectionActivity.this, "No driver near you!", Toast.LENGTH_SHORT).show();
            }
            else {
                SendInforToDriver(driver);
            }
        }
    }



    private class EmitTripLogs extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {

            ConnectionFactory factory = new ConnectionFactory();
            try {
                factory.setUri(AMQP_URL);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            Connection connection = null;
            try {
                connection = factory.newConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            Channel channel = null;
            try {
                channel = connection.createChannel();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.exchangeDeclare(EXCHANGE_NAME_LOCATION, "direct");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.basicPublish(EXCHANGE_NAME_LOCATION, driver.driverID, null, params[0].getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

