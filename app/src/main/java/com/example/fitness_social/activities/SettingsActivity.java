package com.example.fitness_social.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivitySettingsBinding;
import com.example.fitness_social.fragments.MyProfileFragment;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
import com.example.fitness_social.fragments.navigation_bar.LeaderboardFragment;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.fragments.navigation_bar.PlanFragment;

import com.example.fitness_social.tables.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private final LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
    private final PlanFragment planFragment = new PlanFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final MyProfileFragment myProfileFragment = new MyProfileFragment();
    private ActivitySettingsBinding binding;
    private FirebaseUser currentUser;
    private UserInfo currentUserInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        initPage();
        setAllOnClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.navigationBar.setSelectedItemId(R.id.settings);
    }

    private void initPage(){
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
    }
    private void setAllOnClick(){
        setLogoutOnClick();
        setNavBarOnClick();
        setMyProfilenClick();
    }
    private void setMyProfilenClick(){
        LinearLayout myProfileLinearLayout = findViewById(R.id.myProfileSetting);
        myProfileLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("isNewUser","0");
                myProfileFragment.setArguments(bundle);
                LinearLayout logoutLinearLayout = findViewById(R.id.logout);
                logoutLinearLayout.setEnabled(false);
                myProfileLinearLayout.setEnabled(false);
                getSupportFragmentManager().beginTransaction().replace(R.id.settingContainer, myProfileFragment).commitNow();
            }
        });
    }
    private void setLogoutOnClick() {
        LinearLayout logoutLinearLayout = findViewById(R.id.logout);
        logoutLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm logout
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Yes" - proceed with logout
                                currentUserInfo.status = UserInfo.UserInfoStatus.OFFLINE;
                                UserInfo.insertAndUpdate(currentUserInfo);
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                mAuth.signOut();
                                Intent login_intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                startActivity(login_intent);
                                finish(); // Optional: close SettingsActivity
                            }
                        })
                        .setNegativeButton("No", null) // User clicked "No" - do nothing
                        .show();
            }
        });
    }

    private void setNavBarOnClick(){
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.friends) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.settingContainer, leaderboardFragment
                ).commit();
            } else if (item.getItemId() == R.id.plan) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.settingContainer, planFragment
                ).commitNow();
            } else if (item.getItemId() == R.id.achievements) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.settingContainer, achievementsFragment
                ).commit();
            } else if (item.getItemId() == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.settingContainer, homeFragment
                ).commit();
            }
            return true;
        });
    }

    public void termsConditionsOnClick(View view) {
        Intent intent = new Intent(this, TermsConditionsActivity.class);
        startActivity(intent);
    }
}