package com.example.fitness_social.activities;

import static com.example.fitness_social.ActivityConstants.CYCLING_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.CYCLING_RULES;
import static com.example.fitness_social.ActivityConstants.DISTANCE_INFO;
import static com.example.fitness_social.ActivityConstants.INVALID_CYCLING_NOTE1;
import static com.example.fitness_social.ActivityConstants.INVALID_CYCLING_NOTE2;
import static com.example.fitness_social.ActivityConstants.INVALID_PUSHUPS_NOTE2;
import static com.example.fitness_social.ActivityConstants.INVALID_PUSHUPS_NOTE3;
import static com.example.fitness_social.ActivityConstants.INVALID_RUNNING_NOTE1;
import static com.example.fitness_social.ActivityConstants.INVALID_RUNNING_NOTE2;
import static com.example.fitness_social.ActivityConstants.NOPHONE_INFO;
import static com.example.fitness_social.ActivityConstants.NOPHONE_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.NOPHONE_RULES;
import static com.example.fitness_social.ActivityConstants.POINTS_INFO;
import static com.example.fitness_social.ActivityConstants.PUSHUPS_INFO;
import static com.example.fitness_social.ActivityConstants.PUSHUPS_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.PUSHUPS_RULES;
import static com.example.fitness_social.ActivityConstants.RUNNING_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.RUNNING_RULES;
import static com.example.fitness_social.ActivityConstants.SPEED_INFO;
import static com.example.fitness_social.ActivityConstants.SPEED_JUST_RIGHT;
import static com.example.fitness_social.ActivityConstants.SPEED_TOO_FAST;
import static com.example.fitness_social.ActivityConstants.SPEED_TOO_SLOW;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.example.fitness_social.R;
import com.example.fitness_social.services.CyclingService;
import com.example.fitness_social.services.PushUpsService;
import com.example.fitness_social.services.RunningService;
import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserInfo;
import com.example.fitness_social.tables.UserPointHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Start and stop sessions for monitoring progress in selected activity
 */
public class CurrentActivity extends AppCompatActivity {

    // Location permissions
    private static final int LOCATION_PERMISSION_CODE = 1000;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // Leaving screen messages
    private static final String BEFORE_ACTIVITY_NOTE =
            "\nNote: After starting an activity, you won't be able to leave this screen until you stop it";
    private static final String DURING_ACTIVITY_NOTE =
            "\nNote: You can't leave this screen until you stop the current activity";
    private static final String DURING_ACTIVITY_POPUP =
            "You need to stop the current activity before you can leave this screen";

    // System components
    private PowerManager powerManager;

    // UI components
    private TextView activityText;
    private TextView rulesText;
    private TextView timeText;
    private TextView progressText;
    private TextView pointsText;
    private TextView warningText;
    private ImageView activityImage;
    private Button startStopButton;
    private ImageButton backButton;

    // Media components
    private Vibrator vibration;
    private MediaPlayer clickSound;
    private MediaPlayer successSound;

    // Messengers
    private Messenger runningMessenger;
    private Messenger cyclingMessenger;

    // Timer handling
    private static final int SECOND = 1000;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private int ticks;

    // User information
    private FirebaseUser currentUser;
    private UserInfo currentUserInfo;
    private UserDailyPlan currentUserDailyPlan;

    // Activity information
    private String activity;
    private boolean isStarted;
    private boolean isPaused;
    private boolean backButtonClicked = true;
    private int pushUpsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialise screen
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_current);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        // Initialise components
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        activityText = findViewById(R.id.activityText);
        rulesText = findViewById(R.id.rulesText);
        timeText = findViewById(R.id.timeText);
        progressText = findViewById(R.id.progressText);
        pointsText = findViewById(R.id.pointsText);
        warningText = findViewById(R.id.warningText);
        activityImage = findViewById(R.id.activityImage);
        startStopButton = findViewById(R.id.startStopButton);
        backButton = findViewById(R.id.backButton);
        vibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        clickSound = MediaPlayer.create(this, R.raw.click);
        successSound = MediaPlayer.create(this, R.raw.success);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        UserInfo.selectAll(allUserInfo -> {
            for (UserInfo userInfo : allUserInfo) {
                if (Objects.equals(userInfo.uid, currentUser.getUid())) {
                    currentUserInfo = userInfo;
                    Log.d("findUser", currentUserInfo.uid);
                    return;
                }
            }
        });
        bindButtons();
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Restart page if entering from another page (not reopening app or turning screen on)
        if (backButtonClicked) {
            // Prevent clicking start button before user data is retrieved
            startStopButton.setEnabled(false);
            // Initialise UI components
            activity = getIntent().getStringExtra("activity");
            activityText.setText("\n" + activity + "\n");
            timeText.setText("00 : 00 : 00");
            // Display user data
            switch (Objects.requireNonNull(activity)) {
                case "Running":
                    rulesText.setText(RUNNING_RULES);
                    activityImage.setImageResource(R.drawable.run);

                    runningMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            Bundle data = msg.getData();
                            int distance = (int) data.getDouble("distance");
                            long duration = data.getLong("duration");
                            double speed = data.getDouble("speed");
                            int speedStatus = data.getInt("speedStatus");
                            double altitudeDelta = data.getDouble("altitudeDelta");
                            String altitudeStatus = data.getString("altitudeStatus");

                            switch (speedStatus) {
                                case SPEED_TOO_SLOW -> rulesText.setText(INVALID_RUNNING_NOTE1 + String.format(SPEED_INFO, speed));
                                case SPEED_TOO_FAST -> rulesText.setText(INVALID_RUNNING_NOTE2 + String.format(SPEED_INFO, speed));
                                case SPEED_JUST_RIGHT -> rulesText.setText(RUNNING_RULES + String.format(SPEED_INFO, speed));
                            }

                            timeText.setText(formatTime(duration));
                            progressText.setText(String.format(DISTANCE_INFO, (int)distance));
                            pointsText.setText(String.format(POINTS_INFO, (int)(RUNNING_POINTS_SCALE * distance)));
                        }
                    });

                    UserDailyPlan.selectAll(allUserDailyPlan -> {
                        ZonedDateTime nowTime = ZonedDateTime.now();
                        // look for today's plan first
                        for (UserDailyPlan plan : allUserDailyPlan) {
                            if (currentUser.getUid().equals(plan.user_id) &&
                                    plan.activity_name.equals("Running") &&
                                    !plan.status.equals(UserDailyPlan.DailyPlanStatus.FINISH) &&
                                    plan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN)) {

                                System.out.println("plan id: " + plan.daily_plan_id);
                                System.out.println("Plan userid: " + plan.user_id);
                                System.out.println("Plan activity name: " + plan.activity_name);

                                System.out.println("Plan status: " + plan.status);
                                System.out.println("Plan type: " + plan.type);
                                System.out.println("Plan begin time: " + plan.begin_time);
                                System.out.println("Plan end time: " + plan.end_time);

                                // Parse date time
                                ZonedDateTime planBeginTime = ZonedDateTime.parse(plan.begin_time);
                                ZonedDateTime planEndTime = ZonedDateTime.parse(plan.end_time);

                                // Check if the current time is within the plan's time range
                                if (nowTime.isEqual(planBeginTime) ||
                                        (nowTime.isAfter(planBeginTime) && nowTime.isBefore(planEndTime))) {
                                    currentUserDailyPlan = plan;
                                    progressText.setText(String.format(DISTANCE_INFO, currentUserDailyPlan.current));
                                    pointsText.setText(String.format(POINTS_INFO, RUNNING_POINTS_SCALE * currentUserDailyPlan.current));
                                    return;
                                }
                            }
                        }
                        // If no plan is found for today, create a new plan
                        progressText.setText(String.format(DISTANCE_INFO, 0));
                        pointsText.setText(String.format(POINTS_INFO, 0));
                        ZonedDateTime endOfDay = ZonedDateTime.now().withHour(23).withMinute(59).withSecond(59);
                        currentUserDailyPlan = new UserDailyPlan(currentUser.getUid(), "Running",
                                UserDailyPlan.DailyPlanType.DAILY_PLAN, ZonedDateTime.now().toString(),
                                endOfDay.toString(), -1, -1);
                        currentUserDailyPlan.status = UserDailyPlan.DailyPlanStatus.ONGOING;
                        UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
                    });
                    break;
                case "Cycling":
                    activityImage.setImageResource(R.drawable.cycle);
                    rulesText.setText(CYCLING_RULES);

                    // create a Handler to process updates from CyclingService
                    cyclingMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            Bundle data = msg.getData();
                            double distance = data.getDouble("distance");
                            long duration = data.getLong("duration");
                            double speed = data.getDouble("speed");
                            int speedStatus = data.getInt("speedStatus");
                            double altitudeDelta = data.getDouble("altitudeDelta");
                            String altitudeStatus = data.getString("altitudeStatus");

                            switch (speedStatus) {
                                case SPEED_TOO_SLOW -> rulesText.setText(INVALID_CYCLING_NOTE1 + String.format(SPEED_INFO, speed));
                                case SPEED_TOO_FAST -> rulesText.setText(INVALID_CYCLING_NOTE2 + String.format(SPEED_INFO, speed));
                                case SPEED_JUST_RIGHT -> rulesText.setText(CYCLING_RULES + String.format(SPEED_INFO, speed));
                            }

                            timeText.setText(formatTime(duration));
                            progressText.setText(String.format(DISTANCE_INFO, (int)distance));
                            pointsText.setText(String.format(POINTS_INFO, (int)(CYCLING_POINTS_SCALE * distance)));
                        }
                    });

                    // check if there is a plan for today
                    UserDailyPlan.selectAll(new UserDailyPlan.DataCallback() {
                        @Override
                        public void onCallback(ArrayList<UserDailyPlan> allUserDailyPlan) {
                            ZonedDateTime nowTime = ZonedDateTime.now();

                            // look for today's plan first
                            for (UserDailyPlan plan : allUserDailyPlan) {
                                if (currentUser.getUid().equals(plan.user_id) &&
                                        plan.activity_name.equals("Cycling") &&
                                        !plan.status.equals(UserDailyPlan.DailyPlanStatus.FINISH) &&
                                        plan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN)) {

                                    // Parse date time
                                    ZonedDateTime planBeginTime = ZonedDateTime.parse(plan.begin_time);
                                    ZonedDateTime planEndTime = ZonedDateTime.parse(plan.end_time);

                                    // Check if the current time is within the plan's time range
                                    if (nowTime.isEqual(planBeginTime) ||
                                            (nowTime.isAfter(planBeginTime) && nowTime.isBefore(planEndTime))) {
                                        currentUserDailyPlan = plan;
                                        progressText.setText(String.format(DISTANCE_INFO, currentUserDailyPlan.current));
                                        pointsText.setText(String.format(POINTS_INFO, CYCLING_POINTS_SCALE * currentUserDailyPlan.current));
                                        return;
                                    }
                                }
                            }

                            // If no plan is found for today, create a new plan
                            progressText.setText(String.format(DISTANCE_INFO, 0));
                            pointsText.setText(String.format(POINTS_INFO, 0));
                            ZonedDateTime endOfDay = ZonedDateTime.now().withHour(23).withMinute(59).withSecond(59);
                            currentUserDailyPlan = new UserDailyPlan(currentUser.getUid(), "Cycling",
                                    UserDailyPlan.DailyPlanType.DAILY_PLAN, ZonedDateTime.now().toString(),
                                    endOfDay.toString(), -1, -1);
                            currentUserDailyPlan.status = UserDailyPlan.DailyPlanStatus.ONGOING;
                            UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
                        }
                    });
                    break;
                case "Push Ups":
                    activityImage.setImageResource(R.drawable.push_ups);
                    rulesText.setText(PUSHUPS_RULES);
                    displayUserInfo(PUSHUPS_INFO, PUSHUPS_POINTS_SCALE);
                    break;
                case "No Phone Use":
                    activityImage.setImageResource(R.drawable.phone);
                    rulesText.setText(NOPHONE_RULES);
                    displayUserInfo(NOPHONE_INFO, NOPHONE_POINTS_SCALE);
            }
        }
        backButtonClicked = false;
        startStopButton.setEnabled(true);
    }

    @Override
    protected void onStop() {
        if (activity.equals("No Phone Use") && isStarted && powerManager.isInteractive()) {
            stopActivityButton();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // user granted permission, start corresponding service based on current activity type
            switch (activity) {
                case "Running":
                    Intent runningIntent = new Intent(CurrentActivity.this, RunningService.class);
                    runningIntent.putExtra("messenger", runningMessenger);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(runningIntent);
                    } else {
                        startService(runningIntent);
                    }
                    break;
                    
                case "Cycling":
                    Intent cyclingIntent = new Intent(CurrentActivity.this, CyclingService.class);
                    cyclingIntent.putExtra("messenger", cyclingMessenger);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(cyclingIntent);
                    } else {
                        startService(cyclingIntent);
                    }
                    break;
            }
        } else {
            // user denied permission
            Toast.makeText(this, "Location permission is required for tracking. Activity cannot start.", 
                    Toast.LENGTH_LONG).show();
            stopActivityButton();
            isStarted = false;
            
            // if user selected "don't ask again"
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Location permission is required for tracking. Please enable it in Settings.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        // open application settings page
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
            }
        }
    }
}

    /*
    Bind UI buttons to functionality
     */
    private void bindButtons() {
        findViewById(R.id.startStopButton).setOnClickListener(view -> startStopActivity());
        findViewById(R.id.backButton).setOnClickListener(view -> backButton());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backButton();
            }
        });
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d : %02d : %02d", hours, minutes, secs);
    }

    /*
    Retrieve and display user data for current activity
     */
    private void displayUserInfo(String infoText, int pointsScale) {
        UserDailyPlan.selectAll(allUserDailyPlans -> {
            ZonedDateTime nowTime = ZonedDateTime.now();
            // Retrieve today's user record for current activity
            for (UserDailyPlan userDailyPlan : allUserDailyPlans) {
                if (
                        currentUser.getUid().equals(userDailyPlan.user_id) &&
                                userDailyPlan.activity_name.equals(activity) &&
                                !userDailyPlan.status.equals(UserDailyPlan.DailyPlanStatus.FINISH) &&
                                userDailyPlan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN) &&
                                ZonedDateTime.parse(userDailyPlan.begin_time).toLocalDate().equals(nowTime.toLocalDate())
                ) {
                    progressText.setText(String.format(infoText, userDailyPlan.current));
                    pointsText.setText(String.format(POINTS_INFO, pointsScale * userDailyPlan.current));
                    currentUserDailyPlan = userDailyPlan;
                    return;
                }
            }
            // Create new user record for current activity if none exists for today
            progressText.setText(String.format(infoText, 0));
            pointsText.setText(String.format(POINTS_INFO, 0));
            currentUserDailyPlan = new UserDailyPlan(
                    currentUser.getUid(),
                    activity,
                    UserDailyPlan.DailyPlanType.DAILY_PLAN,
                    nowTime.toString(),
                    "",
                    -1,
                    -1
            );
            currentUserDailyPlan.status = UserDailyPlan.DailyPlanStatus.ONGOING;
            UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
        });
    }

    /*
    Handle user clicking start / stop activity button
     */
    private void startStopActivity() {
        // Play vibration and sound effects
        vibration.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        if (clickSound.isPlaying()) {
            clickSound.stop();
            clickSound.release();
            clickSound = MediaPlayer.create(this, R.raw.click);
        }
        clickSound.start();
        // Handle button logic
        if (!isStarted) {
            startActivityButton();
        } else {
            stopActivityButton();
        }
    }

    /*
    Handle user clicking back button for phone or app
     */
    private void backButton() {
        if (!isStarted) {
            backButtonClicked = true;
            finish();
        } else {
            vibration.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            Toast.makeText(getApplicationContext(), DURING_ACTIVITY_POPUP, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Handle user clicking start activity button
     */
    private void startActivityButton() {
        // Reset activity variables
        isStarted = true;
        isPaused = false;
        ticks = 0;
        // Reset UI components
        startStopButton.setBackgroundTintList(ContextCompat.getColorStateList(CurrentActivity.this, R.color.red));
        startStopButton.setText("STOP");
        warningText.setText(DURING_ACTIVITY_NOTE);
        timeText.setText("00 : 00 : 00");
        backButton.setImageResource(R.drawable.back_disabled);
        // Start monitoring activity progress
        runTimer();
        switch (activity) {
            case "Running":
                if (checkLocationPermission()) {
                    Intent runningIntent = new Intent(CurrentActivity.this, RunningService.class);
                    runningIntent.putExtra("messenger", runningMessenger);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(runningIntent);
                    } else {
                        startService(runningIntent);
                    }
                }else{
                    Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show();
                    stopActivityButton(); 
                    isStarted = false;
                    return;
                }
                currentUserInfo.status = UserInfo.UserInfoStatus.RUNNING;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "Cycling":
                if (checkLocationPermission()) {
                    Intent cyclingIntent = new Intent(CurrentActivity.this, CyclingService.class);
                    cyclingIntent.putExtra("messenger", cyclingMessenger);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(cyclingIntent);
                    } else {
                        startService(cyclingIntent);
                    }
                }else{
                    Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show();
                    stopActivityButton(); 
                    isStarted = false;
                    return;
                }
                currentUserInfo.status = UserInfo.UserInfoStatus.CYCLING;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "Push Ups":
                Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        // Update progress and points on screen
                        pushUpsCount = msg.getData().getInt("pushUpsCount");
                        int totalPushUps = currentUserDailyPlan.current + pushUpsCount;
                        progressText.setText(String.format(PUSHUPS_INFO, totalPushUps));
                        pointsText.setText(String.format(POINTS_INFO, PUSHUPS_POINTS_SCALE * totalPushUps));
                        // Alert user if daily goals completed
                        if (totalPushUps == currentUserDailyPlan.requirement) {
                            if (successSound.isPlaying()) {
                                successSound.stop();
                                successSound.release();
                                successSound = MediaPlayer.create(CurrentActivity.this, R.raw.success);
                            }
                            successSound.start();
                            Toast.makeText(CurrentActivity.this, "Daily goal complete!", Toast.LENGTH_LONG).show();
                        }
                        // Change rules text if appropriate
                        if (msg.getData().getBoolean("isTooFast")) {
                            rulesText.setText(INVALID_PUSHUPS_NOTE2);
                        } else if (msg.getData().getBoolean("isTooSlow")) {
                            rulesText.setText(INVALID_PUSHUPS_NOTE3);
                        } else {
                            rulesText.setText(PUSHUPS_RULES);
                        }
                    }
                });
                Intent pushUpsIntent = new Intent(CurrentActivity.this, PushUpsService.class);
                pushUpsIntent.putExtra("messenger", messenger);
                startService(pushUpsIntent);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                currentUserInfo.status = UserInfo.UserInfoStatus.PUSHUPS;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "No Phone Use":
                currentUserInfo.status = UserInfo.UserInfoStatus.NOPHONE;
                UserInfo.insertAndUpdate(currentUserInfo);

        }
    }

    /*
    Handle user clicking stop activity button
     */
    private void stopActivityButton() {
        // Reset variables
        timeHandler.removeCallbacks(null);
        isStarted = false;
        // Reset UI components
        startStopButton.setBackgroundTintList(ContextCompat.getColorStateList(CurrentActivity.this, R.color.green));
        startStopButton.setText("START");
        warningText.setText(BEFORE_ACTIVITY_NOTE);
        backButton.setImageResource(R.drawable.back);
        // Stop monitoring activity progress
        switch (activity) {
            case "Running":
                Intent stopIntent = new Intent(CurrentActivity.this, RunningService.class);
                stopIntent.setAction("STOP_RUNNING");
                startService(stopIntent);

                rulesText.setText(RUNNING_RULES);

                new Handler().postDelayed(() -> {
                    TextView distanceTextView = findViewById(R.id.progressText);
                    String distanceText = distanceTextView.getText().toString();
                    double distanceKm = 0.0;

                    try {
                        distanceText = distanceText.split(": ")[1].replace(" m", "").trim();
                        if (!distanceText.isEmpty()) {
                            distanceKm = Double.parseDouble(distanceText);
                        }
                    } catch (Exception e) {
                        Log.e("RunningData", "Error parsing distance: " + e.getMessage());
                        distanceKm = 0.0;
                    }

                    int distanceMeters = (int) (distanceKm);

                    if (distanceMeters > 0) {
                        currentUserDailyPlan.current += distanceMeters;
                        UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
                        System.out.println("Current User Daily Plan: " + currentUserDailyPlan.current);

                        UserPointHistory userPointHistory1 = new UserPointHistory(currentUserDailyPlan.daily_plan_id, distanceMeters * RUNNING_POINTS_SCALE, ticks);
                        UserPointHistory.insertAndUpdate(userPointHistory1);

                        currentUserInfo.point += userPointHistory1.change;
                        UserInfo.insertAndUpdate(currentUserInfo);

                    }
                }, 0);
                currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "Cycling":
                // REFACTOR???
                Intent stopCyclingIntent = new Intent(CurrentActivity.this, CyclingService.class);
                stopCyclingIntent.setAction("STOP_CYCLING");
                startService(stopCyclingIntent);

                rulesText.setText(CYCLING_RULES);

                new Handler().postDelayed(() -> {
                    TextView distanceTextView = findViewById(R.id.progressText);
                    String distanceText = distanceTextView.getText().toString();
                    double distanceKm = 0.0;

                    try {
                        distanceText = distanceText.split(": ")[1].replace(" m", "").trim();
                        if (!distanceText.isEmpty()) {
                            distanceKm = Double.parseDouble(distanceText);
                        }
                    } catch (Exception e) {
                        Log.e("CyclingData", "Error parsing distance: " + e.getMessage());
                        distanceKm = 0.0;
                    }

                    int distanceMeters = (int)(distanceKm);

                    if (distanceMeters > 0) {
                        System.out.println("begin time:"+currentUserDailyPlan.begin_time);
                        System.out.println("Current User Daily Plan0: " + currentUserDailyPlan.current);
                        System.out.println("Current User Daily requirement: " + currentUserDailyPlan.requirement);
                        System.out.println("Today is: " + ZonedDateTime.now().toLocalDate());
                        currentUserDailyPlan.current += distanceMeters;
                        UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
                        System.out.println("Current User Daily Plan1: " + currentUserDailyPlan.current);

                        UserPointHistory userPointHistory1 = new UserPointHistory(currentUserDailyPlan.daily_plan_id, CYCLING_POINTS_SCALE * distanceMeters, ticks);
                        UserPointHistory.insertAndUpdate(userPointHistory1);
                        System.out.println("User Point History: " + userPointHistory1.change);

                        currentUserInfo.point += userPointHistory1.change;
                        UserInfo.insertAndUpdate(currentUserInfo);

                    }
                }, 0);
                currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "Push Ups":
                stopService(new Intent(CurrentActivity.this, PushUpsService.class));
                rulesText.setText(PUSHUPS_RULES);
                updateData(pushUpsCount, PUSHUPS_POINTS_SCALE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
                UserInfo.insertAndUpdate(currentUserInfo);
                break;
            case "No Phone Use":
                updateData(ticks / 60, NOPHONE_POINTS_SCALE);
                progressText.setText(String.format(NOPHONE_INFO, currentUserDailyPlan.current));
                pointsText.setText(String.format(POINTS_INFO, NOPHONE_POINTS_SCALE * currentUserDailyPlan.current));
                currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
                UserInfo.insertAndUpdate(currentUserInfo);
        }
    }


    private boolean checkLocationPermission() {
        // check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        
        // if user previously denied permission, show explanation dialog
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                .setTitle("Required Location Permission")
                .setMessage("Running and Cycling functions require location permission to track your activity data.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // request permission again
                    requestLocationPermissions();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Unable to use activity tracking", Toast.LENGTH_LONG).show();
                    stopActivityButton();
                    isStarted = false;
                })
                .create()
                .show();
            return false;
        }
        
        // first time request permission
        requestLocationPermissions();
        return false;
    }

    // request permission method
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_CODE);
    }

    /*
    Update activity record based on current progress and points accumulated
     */
    private void updateData(int progress, int pointsScale) {
        if (progress > 0) {
            currentUserDailyPlan.current += progress;
            UserDailyPlan.insertAndUpdate(currentUserDailyPlan);
            UserPointHistory userPointHistory = new UserPointHistory(currentUserDailyPlan.daily_plan_id, pointsScale * progress, ticks);
            UserPointHistory.insertAndUpdate(userPointHistory);
            currentUserInfo.point += userPointHistory.change;
            UserInfo.insertAndUpdate(currentUserInfo);
        }
    }

    /*
    Continuously increment timer by 1 every second until user clicks stop button
     */
    private void runTimer() {
        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isStarted && !isPaused && !activity.equals("Running") && !activity.equals("Cycling")) {
                    ticks++;
                    timeText.setText(String.format("%02d : %02d : %02d", ticks / 3600, (ticks % 3600) / 60, ticks % 60));
                    timeHandler.removeCallbacks(null);
                    timeHandler.postDelayed(this, SECOND);
                }
            }
        }, SECOND);
    }

    /*
    Pause running timer
     */
    private void pauseTimer() {
        isPaused = true;
    }

    /*
    Resume paused timer
     */
    private void resumeTimer() {
        isPaused = false;
        runTimer();
    }

}