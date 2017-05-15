package com.example.trhie.bidariderider;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import dmax.dialog.SpotsDialog;

/**
 * Created by trhie on 4/30/2017.
 */

public class SignupActivity extends AppCompatActivity {
    EditText etFullname, etEmail, etPhone, etUsername, etPassword;
    String fullname, email, phone, username, password;
    String status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFullname = (EditText) findViewById(R.id.fname);
        etEmail = (EditText) findViewById(R.id.mail);
        etPhone = (EditText) findViewById(R.id.phone);
        etUsername = (EditText) findViewById(R.id.usrusr);
        etPassword = (EditText) findViewById(R.id.pswrd);
    }

    public void back(View view) {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
    }

    public void signup(View view) {
        fullname = etFullname.getText().toString();
        email = etEmail.getText().toString();
        phone = etPhone.getText().toString();
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();

        if (fullname.length() == 0 || email.length() == 0 || phone.length() == 0 || username.length() == 0 || password.length() == 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please fill in all information!");
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
            n.execute("https://appluanvan-apigateway.herokuapp.com/user/register", Networking.NETWORK_STATE_SIGNUP);
        }
    }

    public  class Networking extends AsyncTask {
        public static final int NETWORK_STATE_SIGNUP = 2;
        private SpotsDialog progressDialog = new SpotsDialog(SignupActivity.this, R.style.Custom);

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

            if (!status.trim().equals("OK")) {
                Toast.makeText(SignupActivity.this, "Username or Email already exists!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(SignupActivity.this, "Create new account success!", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(SignupActivity.this, SigninActivity.class);
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
            case Networking.NETWORK_STATE_SIGNUP:
                postParameters.add(new BasicNameValuePair("username", username));
                postParameters.add(new BasicNameValuePair("password", password));
                postParameters.add(new BasicNameValuePair("email", email));
                postParameters.add(new BasicNameValuePair("fullname", fullname));
                postParameters.add(new BasicNameValuePair("phone", phone));
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
            status = stringBuffer.toString().trim();
        }
    }
}
