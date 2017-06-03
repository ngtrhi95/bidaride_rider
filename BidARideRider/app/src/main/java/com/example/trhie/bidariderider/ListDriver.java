package com.example.trhie.bidariderider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import Modules.BookingInfo;
import Modules.DriverAdapter;
import Modules.NetworkingCreateTrip;
import Modules.DirectionInfo;
import Modules.Trip;

import static com.example.trhie.bidariderider.UserSession.KEY_FNAME;
import static com.example.trhie.bidariderider.UserSession.KEY_PHONE;
import static com.example.trhie.bidariderider.UserSession.KEY_TOKEN;
import static com.example.trhie.bidariderider.UserSession.PREFER_NAME;

/**
 * Created by trhie on 5/1/2017.
 */

public class ListDriver extends AppCompatActivity{
    public static android.content.SharedPreferences SharedPreferences = null;
    private static String AMQP_URL = "amqp://imtqjgzz:LQWyhmVxKBMgV6ROObew36G07DUs6ZYZ@white-mynah-bird.rmq.cloudamqp.com/imtqjgzz";
    private static String EXCHANGE_NAME_LOCATION = "notification_logs";
    ListView listItems;
    static DirectionInfo directionInfo;
    JSONArray driverObject;
    List<String> lstDriverName = new ArrayList<String>();
    List<String> lstDriverPhone = new ArrayList<String>();
    List<String> lstDriverDistance = new ArrayList<String>();
    ProgressDialog progressDialog;
    Driver driver;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_driver);

        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        Intent i = getIntent();
        directionInfo = (DirectionInfo) i.getParcelableExtra("originPlace");

        String id = directionInfo.getUserID();
        listItems = (ListView) findViewById(R.id.listView);

        NetworkingAPI n = new NetworkingAPI();
        n.execute("https://appluanvan-apigateway.herokuapp.com/api/driver/coordInfo");
    }

    class Driver {
        String driverID;
        String driverFullname;
        String driverPhone;
        float longitude;
        float latitude;
    }

    public class NetworkingAPI extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ListDriver.this, "Please wait.",
                    "Finding driver..!", true);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            getJson((String) params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            setViewListDriver();
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
                    driver = gson.fromJson(mJson, Driver.class);

                    Location locationB = new Location("driver location");
                    locationB.setLatitude(driver.latitude);
                    locationB.setLongitude(driver.longitude);

                    float distance = locationA.distanceTo(locationB);
                    if (distance <= 5000) {
                        lstDriverNearYou.add(driver);
                        if (distance >= 1000){
                            distance = distance/1000;
                            lstDriverDistance.add(new DecimalFormat("##.##").format(distance) + " km");
                        }
                        else {
                            lstDriverDistance.add(new DecimalFormat("##.##").format(distance) + " m");
                        }

                        lstDriverName.add(driver.driverFullname);
                        lstDriverPhone.add(driver.driverPhone);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return lstDriverNearYou;
        }

        void setViewListDriver() {
            final List<Driver> lstDriverNearYou = getListDriverNearOrigin();
            progressDialog.dismiss();
            if(lstDriverName.isEmpty()) {
                showNoticeDialog();
            }
            else {
                DriverAdapter driverAdapter = new DriverAdapter(ListDriver.this, lstDriverName, lstDriverPhone, lstDriverDistance);
                listItems.setAdapter(driverAdapter);

                listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        driver = lstDriverNearYou.get(position);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListDriver.this);
                        alertDialogBuilder.setTitle("Confirm");
                        alertDialogBuilder.setMessage("Driver " + driver.driverFullname + ". Are you sure?");
                        alertDialogBuilder.setNegativeButton("YES",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        final Trip tripInfo = createTripInfo(driver);
                                        Gson gson = new Gson();
                                        String json = gson.toJson(tripInfo);

                                        Toast.makeText(ListDriver.this, "Booking success. Driver will contact you in a few minutes.", Toast.LENGTH_SHORT).show();
                                        /*ListDriver.this.runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                NetworkingCreateTrip networking = new NetworkingCreateTrip(getApplicationContext());
                                                networking.execute("https://appluanvan-apigateway.herokuapp.com/api/trip/create", tripInfo);
                                            }
                                        });*/
                                        EmitLocationLogs notiTask = new EmitLocationLogs();
                                        notiTask.execute(json.toString());

                                        BookingInfo bookingInfo = new BookingInfo();
                                        bookingInfo.setDriverName(driver.driverFullname);
                                        bookingInfo.setFromAddress(directionInfo.getOriginInfo().getAddress());
                                        bookingInfo.setDriverPhone(driver.driverPhone);
                                        bookingInfo.setToAddress(directionInfo.getDesAddress());
                                        bookingInfo.setCost(directionInfo.getCost());
                                        Intent it = new Intent(ListDriver.this, StatusActivity.class);
                                        it.putExtra("bookingInfo", bookingInfo);
                                        startActivity(it);
                                    }
                                });

                        alertDialogBuilder.setPositiveButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            }
        }
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

    private void showNoticeDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListDriver.this);
        alertDialogBuilder.setMessage("No driver near you!");
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent it = new Intent(ListDriver.this, DirectionActivity.class);
                        startActivity(it);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class EmitLocationLogs extends AsyncTask<String, Void, String> {
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

