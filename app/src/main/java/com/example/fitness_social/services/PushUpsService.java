package com.example.fitness_social.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Detect push ups completed by user
 */
public class PushUpsService extends Service implements SensorEventListener {

    // Service components
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private Messenger activityMessenger;

    // Push up data
    private int pushUpCount = 0;
    private boolean isPushUpInProgress;
    private boolean isTooClose;
    private boolean isTooFast;
    private boolean isTooSlow;
    ArrayList<Boolean> isTooCloseHistory = new ArrayList<>();
    ArrayList<Integer> isTooFastOrSlowHistory= new ArrayList<>();
    private float highestPoint = 0.0f;
    ZonedDateTime time1 = ZonedDateTime.now();
    ZonedDateTime time2 = ZonedDateTime.now();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Push","create");
        Collections.addAll(isTooCloseHistory, false, false, false);
        Collections.addAll(isTooFastOrSlowHistory, 0, 0, 0);
        if ((sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE)) != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            activityMessenger = intent.getParcelableExtra("messenger");
            if (proximitySensor != null) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            Log.d("distance",distance+"");
            if (distance < 5.0f && !isPushUpInProgress) {
                isPushUpInProgress = true;
            } else if (distance >= 5.0f && isPushUpInProgress) {
                isPushUpInProgress = false;
                pushUpCount++;
                updateHistory();
                judgeIsStandard();
                highestPoint = 0.0f;
                sendPushUpCountToActivity(pushUpCount);
            } else if (distance >= 5.0f && distance > highestPoint) {
                highestPoint = distance;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    Update history of conditions for push ups
     */
    public void updateHistory() {
        time1 = time2;
        time2 = ZonedDateTime.now();
        long secondsDifference = Duration.between(time1, time2).getSeconds();
        isTooFastOrSlowHistory.remove(0);
        if (secondsDifference < 3) {
            isTooFastOrSlowHistory.add(1);
        }  else if (secondsDifference > 10) {
            isTooFastOrSlowHistory.add(-1);
        } else {
            isTooFastOrSlowHistory.add(0);
        }
        isTooCloseHistory.remove(0);
        if (highestPoint <= 8.0f)
            isTooCloseHistory.add(true);
        else
            isTooCloseHistory.add(false);
    }

    /*
    Determine whether user is doing push ups too close, fast or slow
     */
    public void judgeIsStandard() {
        isTooClose = isTooCloseHistory.get(0) && isTooCloseHistory.get(1) && isTooCloseHistory.get(2);
        if (isTooFastOrSlowHistory.get(0) + isTooFastOrSlowHistory.get(1) + isTooFastOrSlowHistory.get(2) == 3) {
            isTooFast = true;
            isTooSlow = false;
        }
        else if (isTooFastOrSlowHistory.get(0) + isTooFastOrSlowHistory.get(1) + isTooFastOrSlowHistory.get(2) == -3) {
            isTooFast = false;
            isTooSlow = true;
        }
        else {
            isTooFast = false;
            isTooSlow = false;
        }
    }

    /*
    Update UI based on push ups count and conditions
     */
    private void sendPushUpCountToActivity(int count) {
        if (activityMessenger != null) {
            try {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putInt("pushUpsCount", count);
                bundle.putBoolean("isTooClose", isTooClose);
                bundle.putBoolean("isTooFast", isTooFast);
                bundle.putBoolean("isTooSlow", isTooSlow);
                msg.setData(bundle);
                activityMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}