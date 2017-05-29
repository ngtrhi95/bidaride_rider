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
    int status;
    UserSession session;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        session = new UserSession(getApplicationContext());

        etUsername = (EditText)findViewById(R.id.usrusr);
        etPassword = (EditText) findViewById(R.id.pswrd);
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
            n.execute("https://appluanvan-apigateway.herokuapp.com/api/user/login", Networking.NETWORK_STATE_SIGNIN);

        }
    }

    public  class Networking extends AsyncTask {
        public static final int NETWORK_STATE_SIGNIN = 1;
        private SpotsDialog progressDialog = new SpotsDialog(SigninActivity.this, R.style.Custom);
        JSONObject response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                response = getJson((String) params[0], (Integer) params[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressDialog.dismiss();

            try {
                status = response.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (status != 200) {
                Toast.makeText(SigninActivity.this, "Username or Password is incorrect!", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONObject payload = null;
                String uID = "";
                String uPhone = "";
                String uEmail = "";
                String uFname = "";
                String uToken = "";
                try {
                    payload = response.getJSONObject("payload");
                    uID = payload.getString("userID");
                    uPhone = payload.getString("phone");
                    uEmail = payload.getString("email");
                    uFname = payload.getString("userFullname");
                    uToken = response.getString("token");
                } catch (JSONException err) {
                    Log.e("MYAPP", "JSON exception error", err);
                }


                session.createUserLoginSession(username, uID, uPhone, uEmail, uFname,
                        password, uToken);

                Intent it = new Intent(SigninActivity.this, DirectionActivity.class);
                startActivity(it);
            }
        }
    }

    private JSONObject getJson(String url, int state) throws JSONException {
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
        StringBuffer stringBuffer = new StringBuffer("");
        if (valid) {
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

        }
        JSONObject jo = new JSONObject(stringBuffer.toString());
        return jo;
    }
}
