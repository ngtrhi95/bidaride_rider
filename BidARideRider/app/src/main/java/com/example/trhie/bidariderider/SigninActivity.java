package com.example.trhie.bidariderider;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
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

import dmax.dialog.SpotsDialog;

import static android.R.attr.button;

/**
 * Created by trhie on 4/30/2017.
 */

public class SigninActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    String username, password;
    JSONObject userObject;
    int status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        etUsername = (EditText)findViewById(R.id.usrusr);
        etPassword = (EditText) findViewById(R.id.pswrd);
    }

    public void back(View view) {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
    }

    public void signin(View view) {
        username = etUsername.getText()+"";
        password = etPassword.getText()+"";

        if (username.length() == 0 || password.length() == 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please fill in username and password!");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                }
                            });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else {
            Networking n = new Networking();
            n.execute("https://fast-hollows-58498.herokuapp.com/user/login", Networking.NETWORK_STATE_SIGNIN);

        }
    }

    public  class Networking extends AsyncTask {
        public static final int NETWORK_STATE_SIGNIN = 1;
        private SpotsDialog progressDialog = new SpotsDialog(SigninActivity.this, R.style.Custom);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            getJson((String) params[0], (Integer) params[1]);
            return  null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressDialog.dismiss();

            if (status != 200) {
                Toast.makeText(SigninActivity.this, "Username or Password is incorrect!", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent it = new Intent(SigninActivity.this, DirectionActivity.class);
                it.putExtra("userObject", userObject.toString());
                startActivity(it);
            }
        }
    }

    private void getJson(String url, int state) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        boolean valid = false;

        switch (state) {
            case Networking.NETWORK_STATE_SIGNIN:
                postParameters.add(new BasicNameValuePair("username", username));
                postParameters.add(new BasicNameValuePair("password", password));
                valid = true;
                break;
        }

        if (valid) {
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
    }

    private void decodeResultIntoJson(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            userObject = jo.getJSONObject("payload");
            status = jo.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
