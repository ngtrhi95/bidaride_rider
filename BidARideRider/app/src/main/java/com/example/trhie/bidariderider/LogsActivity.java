package com.example.trhie.bidariderider;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.Trip;
import Modules.TripAdapter;


import static com.example.trhie.bidariderider.UserSession.KEY_ID;
import static com.example.trhie.bidariderider.UserSession.KEY_TOKEN;
import static com.example.trhie.bidariderider.UserSession.PREFER_NAME;


/**
 * Created by trhie on 5/31/2017.
 */

public class LogsActivity extends AppCompatActivity {
    public static android.content.SharedPreferences SharedPreferences = null;
    final ArrayList<Trip> listTrip = new ArrayList<Trip>();
    TripAdapter adapter;
    String userID, token;
    JSONArray tripObject;
    ListView listItems;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);


        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);

        userID = SharedPreferences.getString(KEY_ID, "");
        token = SharedPreferences.getString(KEY_TOKEN, "");
        listItems = (ListView)findViewById(R.id.listTrip);
        Networking n = new Networking();
        n.execute("https://appluanvan-apigateway.herokuapp.com/api/trip/getTrip");
    }

    private class Networking  extends AsyncTask {
        JSONObject response;
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                response = getJson((String) params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LogsActivity.this, "Please wait.",
                    "Finding history trip..!", true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressDialog.dismiss();
            setViewListTrip();
        }
    }

    private void setViewListTrip() {

        Gson gson = new Gson();
        for (int i = 0; i < tripObject.length(); i++) {
            try {
                JsonParser parser = new JsonParser();
                JsonElement mJson = parser.parse(tripObject.getString(i));
                Trip trip = gson.fromJson(mJson, Trip.class);
                listTrip.add(trip);
                } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        TripAdapter driverAdapter = new TripAdapter(LogsActivity.this, listTrip);
        listItems.setAdapter(driverAdapter);
    }

    private JSONObject getJson(String url) throws JSONException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        boolean valid = false;
        postParameters.add(new BasicNameValuePair("userID", userID));
        postParameters.add(new BasicNameValuePair("token", token));
        StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader bufferedReader = null;

            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
                request.setEntity(entity);
                HttpResponse response_http = httpClient.execute(request);

                bufferedReader = new BufferedReader(new InputStreamReader(response_http.getEntity().getContent()));

                String line = "";
                String LineSeparator = System.getProperty("line.separator");

                while ((line = bufferedReader.readLine())!= null) {
                    stringBuffer.append(line + LineSeparator);
                }
                bufferedReader.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        JSONObject jo = new JSONObject(stringBuffer.toString());
        decodeResultIntoJson(stringBuffer.toString());
        return jo;
    }

    private void decodeResultIntoJson(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            int status = jo.getInt("status");
            tripObject = jo.getJSONArray("payload");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
