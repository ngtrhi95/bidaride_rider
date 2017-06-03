package Modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.trhie.bidariderider.DirectionActivity;
import com.example.trhie.bidariderider.ListDriver;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.trhie.bidariderider.UserSession.KEY_TOKEN;
import static com.example.trhie.bidariderider.UserSession.PREFER_NAME;

/**
 * Created by trhie on 5/2/2017.
 */

public class NetworkingCreateTrip extends AsyncTask {

    private Context context;

    public NetworkingCreateTrip(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        getJson((String) params[0], (Trip) params[1]);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

    }

    private void getJson(String url, Trip data) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        SharedPreferences prefs = DirectionActivity.SharedPreferences;

        String token = prefs.getString(KEY_TOKEN, "");

        postParameters.add(new BasicNameValuePair("userID", data.getUserID()));
        postParameters.add(new BasicNameValuePair("driverID", data.getDriverID()));
        postParameters.add(new BasicNameValuePair("tripFrom", data.getTripFrom()));
        postParameters.add(new BasicNameValuePair("tripTo", data.getTripTo()));
        postParameters.add(new BasicNameValuePair("fromLong", String.valueOf(data.getFromLong())));
        postParameters.add(new BasicNameValuePair("fromLat", String.valueOf(data.getFromLat())));
        postParameters.add(new BasicNameValuePair("toLong", String.valueOf(data.getToLong())));
        postParameters.add(new BasicNameValuePair("toLat", String.valueOf(data.getToLat())));
        postParameters.add(new BasicNameValuePair("price", String.valueOf(data.getPrice())));
        postParameters.add(new BasicNameValuePair("token", token));

        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer("");
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);

            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

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

        decodeResultIntoJson(stringBuffer.toString());
    }

    private void decodeResultIntoJson(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            int status = jo.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
