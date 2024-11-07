package com.example.fitness_social.activities;

import static com.example.fitness_social.ActivityConstants.CULTURE_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.CYCLING_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.DISTANCE_UNITS;
import static com.example.fitness_social.ActivityConstants.NOPHONE_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.POINTS_UNITS;
import static com.example.fitness_social.ActivityConstants.PUSHUPS_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.PUSHUPS_UNITS;
import static com.example.fitness_social.ActivityConstants.RUNNING_POINTS_SCALE;
import static com.example.fitness_social.ActivityConstants.TIME_UNITS;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivitySelectBinding;
import com.example.fitness_social.tables.UserDailyPlan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.ZonedDateTime;

/*
Select one of five activities (running, cycling, push ups, no phone use, culture) for monitoring progress
 */
public class SelectActivity extends AppCompatActivity {

    private ActivitySelectBinding binding;

    // User data
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        binding.backButton.bringToFront();
        bindButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayUserInfo("Running", DISTANCE_UNITS, RUNNING_POINTS_SCALE, binding.runningScoreText, binding.runningCompleteTag, binding.runningIncompleteTag);
        displayUserInfo("Cycling", DISTANCE_UNITS, CYCLING_POINTS_SCALE, binding.cyclingScoreText, binding.cyclingCompleteTag, binding.cyclingIncompleteTag);
        displayUserInfo("Push Ups", PUSHUPS_UNITS, PUSHUPS_POINTS_SCALE, binding.pushUpsScoreText, binding.pushUpsCompleteTag, binding.pushUpsIncompleteTag);
        displayUserInfo("No Phone Use", TIME_UNITS, NOPHONE_POINTS_SCALE, binding.noPhoneScoreText, binding.noPhoneCompleteTag, binding.noPhoneIncompleteTag);
        displayUserInfo("Culture", TIME_UNITS, CULTURE_POINTS_SCALE, binding.cultureScoreText, binding.cultureCompleteTag, binding.cultureIncompleteTag);
    }

    /*
    Bind UI buttons to functionality
     */
    private void bindButtons() {
        binding.runningButton.setOnClickListener(view -> startActivity(new Intent(this, CurrentActivity.class).putExtra("activity", "Running")));
        binding.cyclingButton.setOnClickListener(view -> startActivity(new Intent(this, CurrentActivity.class).putExtra("activity", "Cycling")));
        binding.pushUpsButton.setOnClickListener(view -> startActivity(new Intent(this, CurrentActivity.class).putExtra("activity", "Push Ups")));
        binding.noPhoneButton.setOnClickListener(view -> startActivity(new Intent(this, CurrentActivity.class).putExtra("activity", "No Phone Use")));
        binding.cultureButton.setOnClickListener(view -> startActivity(new Intent(this, CultureActivity.class)));
        binding.backButton.setOnClickListener(view -> finish());
    }

    /*
    Retrieve and display user data for activity
     */
    private void displayUserInfo(String activity, String units, int pointsScale, TextView progressText, ImageView completeTag, ImageView incompleteTag) {
        completeTag.setVisibility(View.INVISIBLE);
        incompleteTag.setVisibility(View.INVISIBLE);
        UserDailyPlan.selectAll(allUserDailyPlan -> {
            for (UserDailyPlan userDailyPlan : allUserDailyPlan) {
                if (
                        currentUser.getUid().equals(userDailyPlan.user_id) &&
                        userDailyPlan.activity_name.equals(activity) &&
                        !userDailyPlan.status.equals(UserDailyPlan.DailyPlanStatus.FINISH) &&
                        userDailyPlan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN) &&
                        ZonedDateTime.parse(userDailyPlan.begin_time).toLocalDate().equals(ZonedDateTime.now().toLocalDate())
                ) {
                    // Display progress
                    progressText.setText(
                            "Today: " +
                            String.format(units, userDailyPlan.current) +
                            String.format(POINTS_UNITS, pointsScale * userDailyPlan.current)
                    );
                    // Display daily goal completion status
                    if (userDailyPlan.requirement != -1) {
                        (userDailyPlan.current >= userDailyPlan.requirement ? completeTag : incompleteTag).setVisibility(View.VISIBLE);
                    }
                    return;
                }
            }
            progressText.setText("Today: " + String.format(units, 0) + String.format(POINTS_UNITS, 0));
        });
    }

}