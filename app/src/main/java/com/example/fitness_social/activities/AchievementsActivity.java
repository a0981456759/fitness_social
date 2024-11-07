package com.example.fitness_social.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitness_social.PointHistoryDetail;
import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserInfo;
import com.example.fitness_social.tables.UserPointHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivityAchievementsBinding;
import com.example.fitness_social.fragments.MyProfileFragment;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.fragments.navigation_bar.LeaderboardFragment;
import com.example.fitness_social.fragments.navigation_bar.PlanFragment;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;

public class AchievementsActivity extends AppCompatActivity {
    private final LeaderboardFragment friendsFragment = new LeaderboardFragment();
    private final PlanFragment planFragment = new PlanFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final MyProfileFragment myProfileFragment = new MyProfileFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private ActivityAchievementsBinding binding;// Add reference for search

    private TextView totalPointsTextView;
    private TextView achievementProgressTextView;
    private String userId;
    private UserAchievements userAchievements;
    private static HashMap<String, UserDailyPlan> dailyPlanMap = new HashMap<>();
    private static ArrayList<PointHistoryDetail> pointHistoryDetailList = new ArrayList<>();
    private UserInfo userInfo;
    private int culturePoint;
    private int cyclingPoint;
    private int runningPoint;
    private int noPhonePoint;
    private int pushUpPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAchievementsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        super.onCreate(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initPage();
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        //setContentView(R.layout.activity_achievements);  // Replace with your XML layout file name
        // Back button functionality
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());  // Close the activity when back button is pressed

        setNavBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.navigationBar.setSelectedItemId(R.id.achievements);
    }

    private void updateAchievementStatus(String uid){

        dailyPlanMap.clear();
        pointHistoryDetailList.clear();
        UserDailyPlan.selectAll(new UserDailyPlan.DataCallback() {
            @Override
            public void onCallback(ArrayList<UserDailyPlan> allUserDailyPlan) {
                for (int i = 0; i < allUserDailyPlan.size(); i++) {
                    if (Objects.equals(allUserDailyPlan.get(i).user_id, uid)) {
                        dailyPlanMap.put(allUserDailyPlan.get(i).daily_plan_id, allUserDailyPlan.get(i));
                        Log.d(" ",allUserDailyPlan.get(i).daily_plan_id);
                    }
                }
                UserPointHistory.selectAll(new UserPointHistory.DataCallback() {
                    @Override
                    public void onCallback(ArrayList<UserPointHistory> allUserPointHistory) {
                        int flag = 0;
                        for (int i = 0; i < allUserPointHistory.size(); i++) {
                            if (dailyPlanMap.containsKey(allUserPointHistory.get(i).daily_plan_id)) {
                                pointHistoryDetailList.add(new PointHistoryDetail(dailyPlanMap.get(allUserPointHistory.get(i).daily_plan_id), allUserPointHistory.get(i)));
                                Log.d("Find history point ", allUserPointHistory.get(i).user_point_history_id);
                            }
                        }
                        pointHistoryDetailList.sort(Comparator.comparing(o -> o.userPointHistory.change_time));
                        Collections.reverse(pointHistoryDetailList);
                        if (userInfo.achievements_finish_list == null) {
                            userInfo.achievements_finish_list = new ArrayList<>();
                        }

                        String[][] achievementsNames = {
                                {"Culture Curator", "Culture Activity"},
                                {"Locked In", "No Phone Use"},
                                {"Cycling Conqueror", "Cycling"},
                                {"PathFinder", "Running"},
                                {"Push-up Professional", "Push Ups"}
                        };

                        for (String[] achievement : achievementsNames) {
                            String achievementName = achievement[0];
                            String activityName = achievement[1];

                            // Only check if achievement isn't already achieved
                            if (userInfo.achievements_finish_list.indexOf(achievementName) == -1) {
                                int currentPoint = 0;

                                // Calculate total points for the given activity
                                for (int i = 0; i < pointHistoryDetailList.size(); i++) {
                                    UserDailyPlan dailyPlan = pointHistoryDetailList.get(i).userDailyPlan;
                                    UserPointHistory pointHistory = pointHistoryDetailList.get(i).userPointHistory;

                                    // Ensure non-null and activity matches
                                    if (dailyPlan != null && pointHistory != null &&
                                            activityName.equals(dailyPlan.activity_name)) {
                                        currentPoint += pointHistory.change;
                                    }
                                }

                                switch (achievementName) {
                                    case "Culture Curator":
                                        culturePoint = currentPoint;
                                        break;
                                    case "Locked In":
                                        noPhonePoint = currentPoint;
                                        break;
                                    case "Cycling Conqueror":
                                        cyclingPoint = currentPoint;
                                        break;
                                    case "PathFinder":
                                        runningPoint = currentPoint;
                                        break;
                                    case "Push-up Professional":
                                        pushUpPoint = currentPoint;
                                        break;
                                }
                                // Add achievement if points exceed threshold
                                if (currentPoint >= 1000) {
                                    userInfo.achievements_finish_list.add(achievementName);
                                }
                            }else {
                                switch (achievementName) {
                                    case "Culture Curator":
                                        culturePoint = 1000;
                                        break;
                                    case "Locked In":
                                        noPhonePoint = 1000;
                                        break;
                                    case "Cycling Conqueror":
                                        cyclingPoint = 1000;
                                        break;
                                    case "PathFinder":
                                        runningPoint = 1000;
                                        break;
                                    case "Push-up Professional":
                                        pushUpPoint = 1000;
                                        break;
                                }
                            }

                            Log.d("Id",culturePoint + " " + noPhonePoint + " " + cyclingPoint + " " + runningPoint + " " + pushUpPoint);
                            List<Achievement> achievements = Arrays.asList(
                                    new Achievement("Culture Curator", "", culturePoint, 1000, R.drawable.popular),
                                    new Achievement("Locked In", "", noPhonePoint, 1000, R.drawable.challenger),
                                    //new Achievement("Trailblazer", "Travel 20km using the cycling tracker", 7, 7, R.drawable.resilience),
                                    new Achievement("Cycling Conqueror", "", cyclingPoint, 1000, R.drawable.cycling2),
                                    new Achievement("PathFinder", "", runningPoint, 1000, R.drawable.pathfinder),
                                    new Achievement("Push-up Professional", "", pushUpPoint, 1000, R.drawable.strength)
                            );
                            setupAchievement(
                                    findViewById(R.id.achievement_image1),
                                    findViewById(R.id.achievement_title1),
                                    findViewById(R.id.achievement_description1),
                                    findViewById(R.id.completion_indicator1),
                                    findViewById(R.id.progressBar1),
                                    achievements.get(0)
                            );

                            setupAchievement(
                                    findViewById(R.id.achievement_image2),
                                    findViewById(R.id.achievement_title2),
                                    findViewById(R.id.achievement_description2),
                                    findViewById(R.id.completion_indicator2),
                                    findViewById(R.id.progressBar2),
                                    achievements.get(1)
                            );

                            setupAchievement(
                                    findViewById(R.id.achievement_image3),
                                    findViewById(R.id.achievement_title3),
                                    findViewById(R.id.achievement_description3),
                                    findViewById(R.id.completion_indicator3),
                                    findViewById(R.id.progressBar3),
                                    achievements.get(2)
                            );

                            setupAchievement(
                                    findViewById(R.id.achievement_image4),
                                    findViewById(R.id.achievement_title4),
                                    findViewById(R.id.achievement_description4),
                                    findViewById(R.id.completion_indicator4),
                                    findViewById(R.id.progressBar4),
                                    achievements.get(3)
                            );

                            setupAchievement(
                                    findViewById(R.id.achievement_image5),
                                    findViewById(R.id.achievement_title5),
                                    findViewById(R.id.achievement_description5),
                                    findViewById(R.id.completion_indicator5),
                                    findViewById(R.id.progressBar5),
                                    achievements.get(4)
                            );
                        }
                        UserInfo.insertAndUpdate(userInfo);
                    }
                });
            }
        });


    }

    private void initPage(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();
        UserInfo.selectAll(new UserInfo.DataCallback() {
            @Override
            public void onCallback(ArrayList<UserInfo> allUserInfo) {
                UserInfo currentUserInfo;
                int flag = 0;
                for (int i = 0; i < allUserInfo.size(); i++) {
                    if (Objects.equals(allUserInfo.get(i).uid, currentUid)) {
                        currentUserInfo = allUserInfo.get(i);
                        userInfo = currentUserInfo;
                        updateAchievementStatus(currentUid);
                    }
                }
            }
        });
    }


    private void setNavBar(){
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, homeFragment
                ).commit();
            } else if (item.getItemId() == R.id.friends) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, friendsFragment
                ).commit();
            } else if (item.getItemId() == R.id.plan) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, planFragment
                ).commit();
//            } else if (item.getItemId() == R.id.achievements) {
//                getSupportFragmentManager().beginTransaction().replace(
//                        R.id.mainContainer, achievementsFragment
//                ).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, settingsFragment
                ).commit();
            }
            return true;
        });
    }

    private void setupAchievement(
            ImageView imageView,
            TextView titleView,
            TextView descriptionView,
            ImageView completionIndicator,
            ProgressBar progressBar,
            Achievement achievement
    ) {
        double progress = (double) achievement.currentValue() / achievement.totalValue() * 100;
        progressBar.setProgress((int) progress);

        // If achievement is complete, show the completion indicator
        if (achievement.currentValue() >= achievement.totalValue()) {
            completionIndicator.setImageResource(R.drawable.completion);  // Completed icon
        } else {
            completionIndicator.setImageResource(R.drawable.incomplete);  // Incomplete icon
        }
    }

    // Achievement class for holding data
        public record Achievement(String title, String description, int currentValue, int totalValue,
                                  int iconResId) {
    }
}