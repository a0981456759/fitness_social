package com.example.fitness_social.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.fitness_social.R;
import com.example.fitness_social.fragments.LoadingDialogFragment;
import com.example.fitness_social.services.CultureService;
import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserInfo;
import com.example.fitness_social.tables.UserPointHistory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.example.fitness_social.ActivityConstants.CULTURE_INFO;
import static com.example.fitness_social.ActivityConstants.CULTURE_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.POINTS_INFO;

public class CultureActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private Button startButton;
    private TextView timerTextView;
    private boolean isTimerRunning = false;
    private Handler handler;
    private Runnable runnable;
    private long startTime = 0;
    private Map<List<LatLng>, String> googlePolygons = new HashMap<>(); // Polygons representing places
    private boolean isCheckingDistances = false;
    private PlacesClient placesClient;
    private String apiKey = "API_KEY";
    private LoadingDialogFragment loadingDialog = new LoadingDialogFragment();
    private TextView progressText;
    private TextView pointsText;
    private FirebaseUser currentUser;
    private UserInfo currentUserInfo;
    private UserDailyPlan userDailyPlan;
    private Location userLocation;
    private MediaPlayer clickSound;
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("culture", "received");
            if (intent.getAction().equals("com.example.UPDATE_CONDITION")) {
                boolean conditionMet = intent.getBooleanExtra("condition", false);
                Log.d("culture", ""+conditionMet);
                if (conditionMet) {
                    isCheckingDistances = false;
                    stopTimer();
                    showPopup("You have left the activity area for more than " + 100 + "meters.", false);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        com.example.fitness_social.tables.UserInfo.selectAll(new com.example.fitness_social.tables.UserInfo.DataCallback() {
            @Override
            public void onCallback(ArrayList<com.example.fitness_social.tables.UserInfo> allUserInfo) {
                for (int i = 0; i < allUserInfo.size(); i++) {
                    if (Objects.equals(allUserInfo.get(i).uid, currentUser.getUid())) {
                        currentUserInfo = allUserInfo.get(i);
                        Log.d("findUser",currentUserInfo.uid);
                        break;
                    }
                }
            }
        });
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_culture);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.black));

        // Initialize the Places SDK
        Places.initialize(getApplicationContext(), apiKey);
        placesClient = Places.createClient(this);

        startButton = findViewById(R.id.startStopButton);
        timerTextView = findViewById(R.id.timeText);

        // Keep the timer always visible
        timerTextView.setVisibility(View.VISIBLE);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> goBack());

        progressText=findViewById(R.id.progressText);
        pointsText=findViewById(R.id.pointsText);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        IntentFilter filter = new IntentFilter("com.example.UPDATE_CONDITION");
        registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        UserDailyPlan.selectAll(new UserDailyPlan.DataCallback() {
            @Override
            public void onCallback(ArrayList<UserDailyPlan> allUserDailyPlan) {
                ZonedDateTime nowTime = ZonedDateTime.now();
                int flag = 0;
                for (int i =0; i < allUserDailyPlan.size(); i++){
                    UserDailyPlan singleUserDailyPlan = allUserDailyPlan.get(i);
                    if (currentUser.getUid().equals(singleUserDailyPlan.user_id) &&
                            singleUserDailyPlan.activity_name.equals("Culture") &&
                            !singleUserDailyPlan.status.equals(UserDailyPlan.DailyPlanStatus.FINISH) &&
                            singleUserDailyPlan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN) &&
                            ZonedDateTime.parse(singleUserDailyPlan.begin_time).toLocalDate().equals(nowTime.toLocalDate())){
                        userDailyPlan = singleUserDailyPlan;
                        int pointsEarned = (userDailyPlan.current * CULTURE_POINTS_SCALE);
                        progressText.setText(String.format(CULTURE_INFO, userDailyPlan.current));
                        pointsText.setText(String.format(POINTS_INFO,pointsEarned));
                        flag = 1;
                    }
                }
                // First time select this activity today, create a UserDailyPlan
                if (flag == 0){
                    progressText.setText(String.format(CULTURE_INFO, 0));
                    pointsText.setText(String.format(POINTS_INFO,0));
                    userDailyPlan = new UserDailyPlan(currentUser.getUid(), "Culture",
                            UserDailyPlan.DailyPlanType.DAILY_PLAN, ZonedDateTime.now().toString(),
                            "", -1, -1);
                    userDailyPlan.status = UserDailyPlan.DailyPlanStatus.ONGOING;
                    UserDailyPlan.insertAndUpdate(userDailyPlan);
                }
            }
        });

        clickSound = MediaPlayer.create(this, R.raw.click);
    }

    private void goBack() {
        if (!isTimerRunning) {
            finish();
        } else {
            showPopup("You need to stop the current activity before you can leave this page", true);
        }
    }

    public void onStartButtonClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            showPopup("Start failed, please allow location tracking permission",false);
            return;
        }
        // Vibration feedback using VibrationEffect
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)); // Vibrate for 100 milliseconds
        }

        // Sound feedback using MediaPlayer
        if (clickSound != null && clickSound.isPlaying()) {
            clickSound.stop();
            clickSound.release();
        }

        clickSound = MediaPlayer.create(this, R.raw.click); // Load the click sound
        clickSound.start(); // Start playing the sound

        if (isTimerRunning) {
            stopTimer();
        } else {
            loadingDialog.show(getSupportFragmentManager(), "loading");
            searchNearbyPlaces();
        }
        currentUserInfo.status = UserInfo.UserInfoStatus.CULTURE;
        UserInfo.insertAndUpdate(currentUserInfo);
    }

    private void searchNearbyPlaces() {
        // Request user’s current place
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES);

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double userLatitude = location.getLatitude();
                        double userLongitude = location.getLongitude();
                        userLocation = new Location("userLocation");
                        userLocation.setLatitude(userLatitude);
                        userLocation.setLongitude(userLongitude);
                    } else {
                        Log.d("Current Location", "Location is null");
                    }
                })
                .addOnFailureListener(e -> Log.e("Location Error", "Failed to get location", e));


        placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
            googlePolygons.clear();
            final AtomicBoolean foundPlace = new AtomicBoolean(false);

            CountDownLatch latch = new CountDownLatch(response.getPlaceLikelihoods().size());

            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                Place place = placeLikelihood.getPlace();
                String name = place.getName();
                LatLng latLng = place.getLatLng();
                String types = (place.getTypes() != null) ? place.getTypes().toString() : "Unknown Type";
                Log.d("Detected Place", "Place: " + name + ", LatLng: " + latLng + ", Type: " + types);

                if (name != null && (
                        // Check if name contains any of the specified keywords
                        name.toLowerCase().matches(".*(library|park|aquarium|gallery|theatre|bowling|museum|stadium|zoo|beach|guz).*")//guz and google are for debugging
                                ||
                                // Check if types contain any of the specified types
                                types.matches(".*(church|library|mosque|museum|park|zoo).*")
                )) {
                    // Calculate distance
                    Location placeLocation = new Location("placeLocation");
                    placeLocation.setLatitude(latLng.latitude);
                    placeLocation.setLongitude(latLng.longitude);
                    float distanceInMeters = userLocation.distanceTo(placeLocation);
                    Log.d("Place Distance", "Distance to " + name + ": " + distanceInMeters + " meters");
                    if(distanceInMeters<=100){
                        // Create a polygon around this place and add it to googlePolygons
                        fetchBuildingFootprintFromOSM(latLng.latitude, latLng.longitude, name, latch);
                        foundPlace.set(true); // Set foundPlace to true if a matching place is found
                    }else{
                        latch.countDown();
                    }
                } else {
                    // If not a matching place, still count down to avoid deadlocks
                    latch.countDown();
                }
            }

            new Thread(() -> {
                try {
                    latch.await();
                    runOnUiThread(() -> {
                        if (foundPlace.get()) {
                            startTimer();

                            // Convert googlePolygons to a Bundle
                            Bundle bundle = new Bundle();
                            for (Map.Entry<List<LatLng>, String> entry : googlePolygons.entrySet()) {
                                bundle.putParcelableArrayList(entry.getValue(), new ArrayList<>(entry.getKey()));
                            }

                            Intent serviceIntent = new Intent(CultureActivity.this, CultureService.class);
                            serviceIntent.putExtra("googlePolygons", bundle);
                            startService(serviceIntent);

                            loadingDialog.dismiss();
                            showPopup("Start successful! The timer will stop if you leave 100 meters away from the activity place.", true);
                        } else {
                            showPopup("There is no cultural activity place near you", false);
                            loadingDialog.dismiss();
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e("Latch Error", "Error waiting for fetch completion: " + e.getMessage());
                }
            }).start();

        }).addOnFailureListener((exception) -> {
            Log.e("Places API", "Place not found: " + exception.getMessage());
        });
    }

    private void fetchBuildingFootprintFromOSM(double latitude, double longitude, String placeName, CountDownLatch latch) {
        new Thread(() -> {
            boolean polygonFound = false;
            int radius = 5;

            // Set a maximum limit for the radius
            while (!polygonFound && radius <= 1000) {
                try {
                    // Construct the Overpass API URL with the current radius
                    String url = "https://overpass-api.de/api/interpreter?data=[out:json];way[building](around:" + radius + "," + latitude + "," + longitude + ");out geom;";

                    URL requestUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // Parse the Overpass API response to extract the building footprint
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        List<LatLng> buildingFootprint = parseOSMFootprint(jsonResponse);

                        if (buildingFootprint != null && !buildingFootprint.isEmpty()) {
                            polygonFound = true; // A polygon was found
                            runOnUiThread(() -> {
                                // Add the footprint to googlePolygons
                                googlePolygons.put(buildingFootprint, placeName);
                                Log.d("Polygon Added", "Place: " + placeName + " with footprint: " + buildingFootprint);
                            });
                        } else {
                            Log.e("OSM Error", "No building footprint found for radius: " + radius);
                        }
                    } else {
                        Log.e("API Error", "HTTP error code: " + responseCode);
                    }
                } catch (Exception e) {
                    Log.e("API Error", "Error fetching building footprint: " + e.getMessage());
                }

                radius += 5; // Increment the radius
            }

            // Count down the latch regardless of success or failure
            latch.countDown();
        }).start();
    }

    private List<LatLng> parseOSMFootprint(JSONObject osmResponse) {
        List<LatLng> polygon = new ArrayList<>();
        try {
            if (osmResponse.has("elements")) {
                JSONArray elements = osmResponse.getJSONArray("elements");

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i);

                    if (element.has("geometry")) {
                        JSONArray geometry = element.getJSONArray("geometry");
                        for (int j = 0; j < geometry.length(); j++) {
                            JSONObject point = geometry.getJSONObject(j);
                            double lat = point.getDouble("lat");
                            double lon = point.getDouble("lon");
                            polygon.add(new LatLng(lat, lon));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("OSM Error", "Error parsing OSM footprint: " + e.getMessage());
            return null;
        }
        return polygon;
    }

    private void startTimer() {
        isTimerRunning = true;
        startButton.setText("STOP");
        startButton.setBackgroundTintList(ContextCompat.getColorStateList(
                CultureActivity.this, R.color.red)
        );
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setImageResource(R.drawable.back_disabled);

        handler = new Handler();
        startTime = System.currentTimeMillis();

        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedMillis / 1000);
                timerTextView.setText(String.format("%02d : %02d : %02d", seconds/3600, seconds / 60, seconds % 60));
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        handler.post(runnable);
    }

    private void stopTimer() {
        isTimerRunning = false;
        startButton.setText("START");
        startButton.setBackgroundTintList(ContextCompat.getColorStateList(
                CultureActivity.this, R.color.green)
        );
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setEnabled(true);
        backButton.setImageResource(R.drawable.back);

        //Store minutes
        long elapsedMillis = System.currentTimeMillis() - startTime;
        int elapsedSeconds = (int) (elapsedMillis / 1000);
        int elapsedMinutes = elapsedSeconds/60;//rounding here, only count when >1 min
        userDailyPlan.current+=elapsedMinutes;
        int pointsEarned = (userDailyPlan.current*CULTURE_POINTS_SCALE) ;
        progressText.setText(String.format(CULTURE_INFO, userDailyPlan.current));
        pointsText.setText(String.format(POINTS_INFO, pointsEarned));
        UserDailyPlan.insertAndUpdate(userDailyPlan);

        //Store points
        UserPointHistory userPointHistory1 = new UserPointHistory(userDailyPlan.daily_plan_id, pointsEarned,elapsedSeconds);
        UserPointHistory.insertAndUpdate(userPointHistory1);
        currentUserInfo.point += pointsEarned;
        UserInfo.insertAndUpdate(currentUserInfo);

        handler.removeCallbacks(runnable);
        isCheckingDistances = false; // Stop distance checks
        timerTextView.setText("00 : 00 : 00"); // Reset timer display
        Intent intent = new Intent(this, CultureService.class);
        stopService(intent);

        currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
        UserInfo.insertAndUpdate(currentUserInfo);
    }

    private void showPopup(String message, boolean isSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Optional: Log success or error
        Log.d("Popup", isSuccess ? "Success: " + message : "Error: " + message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can continue with location access
                Log.d("Permission", "Location permission granted");
            } else {
                // Permission denied, handle accordingly
                Log.d("Permission", "Location permission denied");
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
    }
}