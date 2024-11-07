package com.example.fitness_social.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.fitness_social.activities.CultureActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class CultureService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Boolean isCheckingDistances=false;
    private Handler handler;
    private Map<List<LatLng>, String> googlePolygons;
    private static final String CHANNEL_ID = "CultureServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the location request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(true)
                .build();

        handler = new Handler(Looper.getMainLooper());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Culture Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("googlePolygons");
            if (bundle != null) {
                googlePolygons = new HashMap<>(); // Initialize your Map
                for (String key : bundle.keySet()) {
                    ArrayList<LatLng> list = bundle.getParcelableArrayList(key);
                    if (list != null) {
                        googlePolygons.put(list, key); // Populate your Map
                    }
                }
            }
        }

        Intent notificationIntent = new Intent(this, CultureActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Culture Service Running")
                .setContentText("Checking distances in the background...")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        // Initialize the LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("service page", "null");
                    return;
                }
                // Get the latest location
                Location location = locationResult.getLastLocation();
                if (location != null && isCheckingDistances) {
                    checkDistances(location,googlePolygons);
                }
            }
        };
        // Start location updates

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

        isCheckingDistances = true;
        return START_STICKY;
    }

    private void checkDistances(Location userLocation, Map<List<LatLng>, String> googlePolygons) {
        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        for (Map.Entry<List<LatLng>, String> entry : googlePolygons.entrySet()) {
            List<LatLng> polygon = entry.getKey();
            String placeName = entry.getValue();

//             Check if the user is within the polygon
            if (PolyUtil.containsLocation(userLatLng, polygon, true)) {
                Log.d("Distance Check", "User is inside the polygon for " + placeName);
                return; // User is inside, do nothing
            } else {
                double minDistance = Double.MAX_VALUE; // Initialize with a large value
                for (LatLng vertex : polygon) {
                    double distance = SphericalUtil.computeDistanceBetween(userLatLng, vertex);
                    if (distance < minDistance) {
                        minDistance = distance; // Update to the smallest distance found
                    }
                }
                Log.d("Distance Check", "Distance to " + placeName + ": " + minDistance);// Use first point as reference
                if (minDistance < 100) {
                    return;
                    // Stop timer and distance check
                }
            }
        }
        stopTimer();
    }

    private void stopTimer() {
        isCheckingDistances = false;
        // Send broadcast to notify activity to stop distance checking
        Intent broadcastIntent = new Intent("com.example.UPDATE_CONDITION");
        broadcastIntent.putExtra("condition", true);
        sendBroadcast(broadcastIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding
    }
}
