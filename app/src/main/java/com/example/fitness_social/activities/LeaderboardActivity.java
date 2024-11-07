package com.example.fitness_social.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivityFriendsLeaderboardBinding;
import com.example.fitness_social.tables.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import com.example.fitness_social.databinding.ActivityFriendsLeaderboardBinding;
import com.example.fitness_social.fragments.MyProfileFragment;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
import com.example.fitness_social.fragments.navigation_bar.LeaderboardFragment;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.fragments.navigation_bar.PlanFragment;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;
import android.content.Context;
import android.content.Intent;


class User {
    private String name;
    private int points;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, int points) {
        this.name = name;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private FirebaseAuth mAuth;
    private EditText searchEditText;


    private final LeaderboardFragment friendsFragment = new LeaderboardFragment();
    private final PlanFragment planFragment = new PlanFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final MyProfileFragment myProfileFragment = new MyProfileFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private ActivityFriendsLeaderboardBinding binding;// Add reference for search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityFriendsLeaderboardBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        super.onCreate(savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        recyclerView = findViewById(R.id.leaderboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEditText = findViewById(R.id.searchEditText); // Initialize search bar
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                return true;
            }
            return false;
        });
        // Set hint to be shown initially
        searchEditText.setHint("Search for a user");

        // Clear the search text when clicked or focused
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText(""); // Clear text when clicked
                searchEditText.setHint(""); // Optionally clear hint
            }
        });

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchEditText.setText(""); // Clear text when focused
                    searchEditText.setHint(""); // Optionally clear hint
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        fetchUserDataAndSetupLeaderboard();

        setNavBar();

        // Add search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.navigationBar.setSelectedItemId(R.id.friends);
    }

    private void setNavBar(){
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, homeFragment
                ).commit();
//            } else if (item.getItemId() == R.id.friends) {
//                getSupportFragmentManager().beginTransaction().replace(
//                        R.id.mainContainer, friendsFragment
//                ).commit();
            } else if (item.getItemId() == R.id.plan) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, planFragment
                ).commit();
            } else if (item.getItemId() == R.id.achievements) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, achievementsFragment
                ).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.mainContainer, settingsFragment
                ).commit();
            }
            return true;
        });
    }

    private void fetchUserDataAndSetupLeaderboard() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : null;

        UserInfo.selectAll(new UserInfo.DataCallback() {
            @Override
            public void onCallback(ArrayList<UserInfo> allUserInfo) {
                UserInfo currentUserInfo = null;

                // Sort allUserInfo by points in descending order
                allUserInfo.sort((u1, u2) -> Integer.compare(u2.point, u1.point));

                // Find the current user's info for logging (optional)
                for (UserInfo userInfo : allUserInfo) {
                    if (currentUid != null && currentUid.equals(userInfo.uid)) {
                        currentUserInfo = userInfo;
                        break;
                    }
                }
// hello
                // Log current user's status and points
                if (currentUserInfo != null) {
                    String status = currentUserInfo.status.toString();
                    int points = currentUserInfo.point;
                    Log.d("CurrentUserInfo", "Status: " + status + ", Points: " + points);
                }

                // Initialize the adapter with sorted users’ data
                adapter = new LeaderboardAdapter(allUserInfo);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        private ArrayList<UserInfo> userList;
        private ArrayList<UserInfo> userListFull;
        private Context context;

        public LeaderboardAdapter(ArrayList<UserInfo> userList) {
            this.userList = new ArrayList<>(userList);
            this.userListFull = new ArrayList<>(userList);
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_friends_leaderboard_recyclerview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserInfo userInfo = userList.get(position);

            holder.userName.setText(userInfo.user_name);
            holder.userStatus.setText(userInfo.status.toString());
            holder.userPoints.setText(String.valueOf(userInfo.point));

            // Load the profile image using Glide
            Glide.with(holder.itemView.getContext())
                    .load(userInfo.profile_url) // Ensure this URL is valid
                    .placeholder(R.drawable.default_profile) // Placeholder while loading
                    .error(R.drawable.default_profile) // Fallback if the image fails to load
                    .circleCrop() // Optional, for circular images
                    .into(holder.userProfileImage);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), ProfileActivity.class);
                intent.putExtra("uid", userInfo.uid);
                holder.itemView.getContext().startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public void filter(String text) {
            userList.clear();
            if (text.isEmpty()) {
                userList.addAll(userListFull);
            } else {
                text = text.toLowerCase();
                for (UserInfo userInfo : userListFull) {
                    if (userInfo.user_name.toLowerCase().contains(text)) {
                        userList.add(userInfo);
                    }
                }
            }
            notifyDataSetChanged();
        }

        // ViewHolder class
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName, userPoints;
            TextView userStatus;
            ImageView userProfileImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.user_name);
                userStatus = itemView.findViewById(R.id.user_status);
                userPoints = itemView.findViewById(R.id.user_points);
                userProfileImage = itemView.findViewById(R.id.profile_image);
            }
        }
    }
}