package com.example.fitness_social.activities;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import com.example.fitness_social.adapters.DailyPlanProgressAdapter;
import com.example.fitness_social.fragments.MyProfileFragment;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
import com.example.fitness_social.fragments.navigation_bar.LeaderboardFragment;
import com.example.fitness_social.fragments.navigation_bar.PlanFragment;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;
import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivityHomeBinding;
import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserInfo;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * See welcome message and overview of progress in daily goals
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // Navbar fragments
    private final MyProfileFragment myProfileFragment = new MyProfileFragment();
    private final LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
    private final PlanFragment planFragment = new PlanFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private DailyPlanProgressAdapter dailyPlanAdapter;

    // User data
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        authenticationDetection();
        initPage();
        bindButtons();
        setNavBar();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        dailyPlanAdapter = new DailyPlanProgressAdapter(new ArrayList<>());
        binding.todayPlansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.todayPlansRecyclerView.setAdapter(dailyPlanAdapter);
        
        // load today's plans
        loadTodayPlans();
    }

    private void loadTodayPlans() {
        if (currentUser == null) return;

        UserDailyPlan.selectAll(allPlans -> {
            List<UserDailyPlan> todayPlans = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            for (UserDailyPlan plan : allPlans) {
                if (plan.user_id.equals(currentUser.getUid())) {
                    try {
                        LocalDate planDate = LocalDate.parse(plan.begin_time.split("T")[0]);
                        if (planDate.equals(today) && plan.requirement > 0) {
                            todayPlans.add(plan);
                        }
                    } catch (Exception e) {
                        Log.e("HomeActivity", "Error parsing date: " + e.getMessage());
                    }
                }
            }
            
            runOnUiThread(() -> {
                if (todayPlans.isEmpty()) {
                    Log.d("HomeActivity", "No plans for today");
                } else {
                    Log.d("HomeActivity", "Found " + todayPlans.size() + " plans for today");
                    dailyPlanAdapter.updateData(todayPlans);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayPlans();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.navigationBar.setSelectedItemId(R.id.home);
    }

    // Ensure user is logged in and redirect them to login page if not
    private void authenticationDetection() {
        if ((currentUser = FirebaseAuth.getInstance().getCurrentUser()) == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        else {
            reload();
        }
    }

    /*
    Retrieve and display user information
     */
    private void initPage() {
        HomeActivity homeActivity = this;
        UserInfo.selectAll(allUserInfo -> {
            for (UserInfo currentUserInfo : allUserInfo) {
                if (Objects.equals(currentUserInfo.uid, currentUser.getUid())) {
                    binding.nameText.setText(currentUserInfo.user_name);
                    String imageUrl = currentUserInfo.profile_url;
                    if (!imageUrl.isEmpty()) {
                        Glide.with(homeActivity).load(imageUrl).into(binding.userImage);
                    }
                    currentUserInfo.status = UserInfo.UserInfoStatus.ONLINE;
                    UserInfo.insertAndUpdate(currentUserInfo);
                    return;
                }
            }
            // Set up new user if does not exist
            binding.navigationBar.setVisibility(GONE);
            Bundle bundle = new Bundle();
            bundle.putString("isNewUser","1");
            myProfileFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, myProfileFragment).commit();
        });
    }

    /*
    Bind UI buttons to functionality
     */
    private void bindButtons() {
        binding.activityButton.setOnClickListener(view -> startActivity(new Intent(this, SelectActivity.class)));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
            }
        });
        binding.userImage.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("uid", currentUser.getUid());
            this.startActivity(intent);
        });
    }

    /*
    Bind UI buttons on navbar to navigation functionality
     */
    private void setNavBar() {
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.friends) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.homeContainer, leaderboardFragment
                ).commit();
            } else if (item.getItemId() == R.id.plan) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.homeContainer, planFragment
                ).commitNow();
            } else if (item.getItemId() == R.id.achievements) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.homeContainer, achievementsFragment
                ).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.homeContainer, settingsFragment
                ).commit();
            }
            return true;
        });
    }

    /*
    Refresh FirebaseAuth if required
     */
    private void reload() {
        FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Main", "reload", task.getException());
            } else {
                Log.e("Main", "reload", task.getException());
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

}