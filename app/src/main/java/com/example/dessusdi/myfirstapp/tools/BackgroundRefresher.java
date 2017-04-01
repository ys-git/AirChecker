package com.example.dessusdi.myfirstapp.tools;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dessusdi.myfirstapp.MainActivity;
import com.example.dessusdi.myfirstapp.R;
import com.example.dessusdi.myfirstapp.models.air_quality.GlobalObject;
import com.example.dessusdi.myfirstapp.models.air_quality.WaqiObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundRefresher extends Service {

    public static final int delay               = 1800000; // Delay between each search query in ms (15 min here)
    public static final int limit               = 100; // Trigger notification when limit reached
    private Handler mHandler                    = new Handler();
    private Timer mTimer                        = null;
    private List<WaqiObject> cities             = new ArrayList<>();
    private List<Integer> notificationsFired    = new ArrayList<>();
    private RequestQueue reQueue;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer();

        this.notificationsFired.clear();
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 1000, delay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        Toast.makeText(this, R.string.service_killed, Toast.LENGTH_SHORT).show();
    }

    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // Fetching data...
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BackgroundRefresher.this, R.string.service_checking, Toast.LENGTH_SHORT).show();
                    cities.clear();
                    cities.addAll(WaqiObject.listAll(WaqiObject.class));

                    Log.d("BACKGROUND", "Checking cities on background task...");
                    for (WaqiObject cityObject : cities) {
                        this.retrieveAirQuality(cityObject.getIdentifier());
                    }
                }

                private void retrieveAirQuality(final int identifier) {
                    reQueue = Volley.newRequestQueue(BackgroundRefresher.this);
                    StringRequest request = new StringRequest(com.android.volley.Request.Method.GET,
                            RequestBuilder.buildAirQualityURL(identifier),
                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Gson gson = new Gson();
                                    GlobalObject global = gson.fromJson(response, GlobalObject.class);
                                    int threshold = global.getRxs().getObs().get(0).getMsg().getAqi();

                                    if (threshold >= limit) {
                                        if (!notificationsFired.contains(identifier)) {
                                            sendAlertPushNotification(global.getRxs().getObs().get(0).getMsg().getCity().getName(), threshold);
                                            Log.d("BACKGROUND", "Notification fired !");
                                            notificationsFired.add(identifier);
                                        }
                                    } else {
                                        // Removing identifier
                                        if (notificationsFired.contains(identifier))
                                            notificationsFired.remove(identifier);
                                    }

                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("BACKGROUND", "Rate limit exceeded, trying again in " + delay);
                                }
                            });

                    try {
                        reQueue.add(request);
                    } catch (Exception e) {
                    }
                }

                private void sendAlertPushNotification(String city, int threshold) {
                    NotificationCompat.Builder b = new NotificationCompat.Builder(BackgroundRefresher.this);

                    Integer pushIdentifier = (int) (long) System.currentTimeMillis() / 1000;

                    Intent notificationIntent = new Intent(BackgroundRefresher.this, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(BackgroundRefresher.this, 0, notificationIntent, 0);

                    b.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.push_alert_title))
                            .setContentText(String.format(getString(R.string.push_alert_content), city, threshold))
                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                            .setContentIntent(contentIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(pushIdentifier, b.build());
                }
            });
        }
    }
}