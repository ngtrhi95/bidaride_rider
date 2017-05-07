package com.example.trhie.bidariderider;

import android.app.ProgressDialog;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Modules.DriverAdapter;
import Modules.NetworkingCreateTrip;
import Modules.DirectionInfo;
import Modules.TripInfo;

/**
 * Created by trhie on 5/1/2017.
 */

public class ListDriver extends AppCompatActivity{
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

        Intent i = getIntent();
        directionInfo = (DirectionInfo) i.getParcelableExtra("originPlace");

        String id = directionInfo.getUserID();
        listItems = (ListView) findViewById(R.id.listView);

        NetworkingAPI n = new NetworkingAPI();
        n.execute("https://fast-hollows-58498.herokuapp.com/driver/coordInfo");
    }

    class Driver {
        String driverID;
        String driverFullname;
        String driverPhone;
        float longitude;
        float latitude;
    }

    public class NetworkingAPI extends AsyncTask{
        public static final int NETWORK_STATE_GETDRIVERS = 3;

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
            super.onPostExecute(o);
            setViewListDriver();
        }

        private void getJson(String url) {
            BufferedReader in = null;
            String data = null;

            try {
                HttpClient client = new DefaultHttpClient();

                URI website = new URI(url);
                HttpGet request = new HttpGet();
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
                int status1 = jo.getInt("status");
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
                        final Driver temp = lstDriverNearYou.get(position);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListDriver.this);
                        alertDialogBuilder.setTitle("Confirm");
                        alertDialogBuilder.setMessage("Driver " + temp.driverFullname + ". Are you sure?");
                        alertDialogBuilder.setNegativeButton("YES",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        TripInfo tripInfo = createTripInfo(temp);

                                        NetworkingCreateTrip networking = new NetworkingCreateTrip();
                                        networking.execute("https://fast-hollows-58498.herokuapp.com/trip/create", tripInfo);
                                        Toast.makeText(ListDriver.this, "Booking success. Driver will contact you in a few minutes.", Toast.LENGTH_SHORT).show();
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

    private TripInfo createTripInfo(Driver temp) {
        TripInfo tripInfo = new TripInfo();
        tripInfo.setUserID(directionInfo.getUserID());
        tripInfo.setDriverID(temp.driverID);
        tripInfo.setTripFrom(directionInfo.getOriginInfo().getAddress());
        tripInfo.setTripTo(directionInfo.getDesAddress());
        tripInfo.setFromLong(Double.toString(directionInfo.getOriginInfo().getLocation().longitude));
        tripInfo.setFromLat(Double.toString(directionInfo.getOriginInfo().getLocation().latitude));
        tripInfo.setToLong(Double.toString(directionInfo.getDesLocation().longitude));
        tripInfo.setToLat(Double.toString(directionInfo.getDesLocation().latitude));
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
}

