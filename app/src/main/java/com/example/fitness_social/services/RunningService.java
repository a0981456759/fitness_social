package com.example.fitness_social.services;
import static com.example.fitness_social.ActivityConstants.SPEED_JUST_RIGHT;
import static com.example.fitness_social.ActivityConstants.SPEED_TOO_FAST;
import static com.example.fitness_social.ActivityConstants.SPEED_TOO_SLOW;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Messenger;
import android.os.Message;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import android.os.Looper;
import android.util.Log;
import com.example.fitness_social.R;

public class RunningService extends Service implements LocationListener {
    private boolean isNormalStop = false;
    private boolean isPaused = false; // Add a variable to track pause status
    private Messenger activityMessenger;
    private LocationManager locationManager;
    private long startTime;
    private long accumulatedDuration = 0;
    private double speed = 0;
    private double altitudeDelta = 0;
    private String altitudeStatus = "Flat";
    private double distance = 0;
    private Location lastLocation;
    private double lastAltitude = 0;
    private static final double MIN_SPEED_THRESHOLD = 5.0f;
    private static final double MAX_SPEED_THRESHOLD = 15.0f;

    private static final String CHANNEL_ID = "RunningServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final double MIN_ACCURACY = 100.0f;
    private static final double MIN_DISTANCE = 1.0f;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityMessenger = intent.getParcelableExtra("messenger");
        if (intent != null) {
            String action = intent.getAction();
            if ("STOP_RUNNING".equals(action)) {
                isNormalStop = true;
                stopRunning();
                return START_NOT_STICKY;
            } else if ("PAUSE_RUNNING".equals(action)) {
                pauseRunning();
                return START_STICKY;
            } else if ("RESUME_RUNNING".equals(action)) {
                resumeRunning();
                return START_STICKY;
            }
        }

        startTime = SystemClock.elapsedRealtime();
        requestLocationUpdates();
        startForeground(NOTIFICATION_ID, createNotification());

        return START_STICKY;
    }

    private void requestLocationUpdates() {
        try {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            } else {
                Log.e("RunningService", "Location permission not granted");
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        } catch (SecurityException e) {
            Log.e("RunningService", "Security exception when requesting location updates", e);
            Toast.makeText(this, "Error accessing location", Toast.LENGTH_LONG).show();
            stopSelf();
        } catch (IllegalArgumentException e) {
            Log.e("RunningService", "GPS provider not available", e);
            Toast.makeText(this, "GPS not available", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void pauseRunning() {
        isPaused = true;
        accumulatedDuration += (SystemClock.elapsedRealtime() - startTime) / 1000;
        locationManager.removeUpdates(this);
        updateNotification();
    }

    private void resumeRunning() {
        if (isPaused) {
            isPaused = false;
            startTime = SystemClock.elapsedRealtime();
            requestLocationUpdates();
        }
    }

//     @Override
// <<<<<<< Howard_Nov2
//     public void onLocationChanged(Location location) {
//         if (isPaused) return;
    
// =======
    public void onLocationChanged(@NonNull Location location) {
        if (isPaused) return; // If paused, do not update location
        if (location.getAccuracy() <= MIN_ACCURACY) {
            int speedStatus = 0;
            if (lastLocation == null) {
                lastLocation = location;
                lastAltitude = location.getAltitude();
            } else {
                double distanceDelta = location.distanceTo(lastLocation);
                double currentSpeed = location.getSpeed() * 3.6; // convert to km/h
                
                // check speed and set status
                if (currentSpeed < MIN_SPEED_THRESHOLD) {
                    speedStatus = SPEED_TOO_SLOW;
                    Log.d("RunningService", "Speed too slow: " + currentSpeed + " km/h");
                } else if (currentSpeed > MAX_SPEED_THRESHOLD) {
                    speedStatus = SPEED_TOO_FAST;
                    Log.d("RunningService", "Speed too fast: " + currentSpeed + " km/h");
                } else {
                    speedStatus = SPEED_JUST_RIGHT;
                    Log.d("RunningService", "Speed just right: " + currentSpeed + " km/h");
                    // only add distance when speed is suitable and distance is greater than the minimum threshold
                    if (distanceDelta > MIN_DISTANCE) {
                        distance += distanceDelta;
                        speed = currentSpeed;
                        System.out.println("distance11: " + distance);
                    }
                }
    
                // update location and altitude related data
                lastLocation = location;
                double currentAltitude = location.getAltitude();
                altitudeDelta = currentAltitude - lastAltitude;
    
                if (currentAltitude > lastAltitude) {
                    altitudeStatus = "Ascending";
                    Log.d("RunningService", "Ascending " + altitudeDelta);
                } else if (currentAltitude < lastAltitude) {
                    altitudeStatus = "Descending";
                    Log.d("RunningService", "Descending " + Math.abs(altitudeDelta));
                }
                lastAltitude = currentAltitude;
            }
    
            // calculate duration and send update
            long currentDuration = (SystemClock.elapsedRealtime() - startTime) / 1000;
            long totalDuration = accumulatedDuration + currentDuration;
            sendUpdateToActivity(distance, totalDuration, speed, altitudeDelta, altitudeStatus, speedStatus);
            updateNotification();
        }
    }

    public void stopRunning() {
        isNormalStop = true;
        accumulatedDuration += (SystemClock.elapsedRealtime() - startTime) / 1000;
        stopForeground(true);
        stopSelf();
    }

//    private void resetData() {
//        System.out.println("resetData");
//        distance = 0;
//        speed = 0;
//        altitudeDelta = 0;
//        altitudeStatus = "Flat";
//        lastLocation = null;
//        lastAltitude = 0;
//        sendUpdateToActivity(distance, 0, speed, altitudeDelta, altitudeStatus);
//    }

    private void updateNotification() {
        long duration = accumulatedDuration;
        if (!isPaused) {
            duration += (SystemClock.elapsedRealtime() - startTime) / 1000;
        }
        Notification notification = createNotification();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification() {
        long duration = accumulatedDuration;
        if (!isPaused) {
            duration += (SystemClock.elapsedRealtime() - startTime) / 1000;
        }
        String content = String.format("Distance: %.2f, Time: %d s, ", distance, duration);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Running" + (isPaused ? " (Paused)" : ""))
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Running Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void sendUpdateToActivity(double distance, long duration, double speed, double altitudeDelta, String altitudeStatus, int speedStatus) {
        if (activityMessenger != null) {
            try {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putDouble("distance", distance);
                bundle.putLong("duration", duration);
                bundle.putDouble("speed", speed);
                bundle.putDouble("altitudeDelta", altitudeDelta);
                bundle.putString("altitudeStatus", altitudeStatus);
                bundle.putInt("speedStatus", speedStatus);
                msg.setData(bundle);
                activityMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e("RunningService", "Error sending message to activity", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}