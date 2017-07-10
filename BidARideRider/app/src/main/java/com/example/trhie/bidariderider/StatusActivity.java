package com.example.trhie.bidariderider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import Modules.BookingInfo;
import Modules.DirectionInfo;
import Modules.Trip;

import static com.example.trhie.bidariderider.UserSession.KEY_ID;
import static com.example.trhie.bidariderider.UserSession.PREFER_NAME;

/**
 * Created by trhie on 5/13/2017.
 */

public class StatusActivity extends AppCompatActivity {
    public static android.content.SharedPreferences SharedPreferences = null;
    private static String EXCHANGE_NAME_LOCATION = "notification_logs";
    private static String AMQP_URL = "amqp://imtqjgzz:LQWyhmVxKBMgV6ROObew36G07DUs6ZYZ@white-mynah-bird.rmq.cloudamqp.com/imtqjgzz";
    TextView tvStatus;
    BookingInfo bookingInfo;
    String userID;
    CountDownTimer countDown = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }
        @Override
        public void onFinish() {
            String status = (String) tvStatus.getText();
            tvStatus.setText("Cancel");

            Toast.makeText(StatusActivity.this, "Driver don't confirm your trip. Please find another driver!", Toast.LENGTH_SHORT).show();

            this.cancel();
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences = getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        String now = DateFormat.getDateTimeInstance().format(new Date());
        setTitle(now);
        setContentView(R.layout.activity_status);

        Intent i = getIntent();
        bookingInfo = (BookingInfo) i.getParcelableExtra("bookingInfo");

        TextView tvHeader = (TextView)findViewById(R.id.tvheader);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/28 Days Later.ttf");
        tvHeader.setTypeface(custom_font);

        ((TextView)findViewById(R.id.tvStatusDriverName)).setText(bookingInfo.getDriverName());
        ((TextView)findViewById(R.id.tvStatusDriverPhone)).setText(bookingInfo.getDriverPhone());
        ((TextView)findViewById(R.id.tvfrom)).setText(bookingInfo.getFromAddress());
        ((TextView)findViewById(R.id.tvto)).setText(bookingInfo.getToAddress());

        tvStatus = (TextView)findViewById(R.id.tvStatusValue);
        ((TextView)findViewById(R.id.tvStatusCost)).setText(String.valueOf(bookingInfo.getCost()) + " VNĐ");
        userID = SharedPreferences.getString(KEY_ID, "");
        /*ReceiveDirectLog tripTask = new ReceiveDirectLog();
        tripTask.execute(driverID);*/
        ReceiveMessage tripTask = new ReceiveMessage();
        tripTask.execute(userID);
    }

    private class ReceiveMessage extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
           countDown.start();
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
            String queueName = null;
            try {
                queueName = channel.queueDeclare().getQueue();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                channel.queueBind(queueName, EXCHANGE_NAME_LOCATION, params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    final String message = new String(body, "UTF-8");

                    Gson g = new Gson();
                    Trip data = g.fromJson(message, Trip.class);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(StatusActivity.this)
                                    .setSmallIcon(R.drawable.logo)
                                    .setContentTitle("Notification")
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setContentText(data.getDriverFullname() + " accept your trip");
                    //Intent resultIntent = new Intent(getApplicationContext(), StatusActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(StatusActivity.this);
                    stackBuilder.addParentStack(StatusActivity.class);
                    //stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(0, mBuilder.build());

                    StatusActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Accept");
                            countDown.cancel();
                        }
                    });
                }
            };
            try {
                channel.basicConsume(queueName, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent it = new Intent(StatusActivity.this, DirectionActivity.class);
        startActivity(it);
    }
}
