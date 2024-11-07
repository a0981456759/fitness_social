package com.example.fitness_social.activities;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class UserAchievements {
    public String userId;  // Unique ID for the user
    public int totalPoints; // Total points earned by the user
    public HashMap<String, AchievementProgress> achievements; // Map of achievement ID to its progress

    public UserAchievements() {
        // Default constructor required for calls to DataSnapshot.getValue(UserAchievements.class)
        this.achievements = new HashMap<>();
    }

    public UserAchievements(String userId) {
        this.userId = userId;
        this.totalPoints = 0;
        this.achievements = new HashMap<>();
    }

    // Method to update user's progress on an achievement
    public void updateAchievement(String achievementId, int progressIncrement) {
        AchievementProgress achievementProgress = achievements.get(achievementId);
        if (achievementProgress == null) {
            achievementProgress = new AchievementProgress(achievementId, 0, 1000); // Assuming a total of 1000 for new achievements
            achievements.put(achievementId, achievementProgress);
        }

        achievementProgress.currentValue += progressIncrement;
        totalPoints += progressIncrement; // Update total points

        // Check for completion
        if (achievementProgress.currentValue >= achievementProgress.totalValue) {
            Log.d("UserAchievements", "Achievement completed: " + achievementId);
        }

        // Update database
        saveToDatabase();
    }

    // Save user achievements to Firebase
    private void saveToDatabase() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("userAchievements").child(userId).setValue(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UserAchievements", "User achievements updated successfully.");
                    } else {
                        Log.d("UserAchievements", "Failed to update achievements: " + task.getException());
                    }
                });
    }

    // Fetch user's achievements from Firebase
    public static void fetchUserAchievements(String userId, DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("userAchievements").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAchievements userAchievements = dataSnapshot.getValue(UserAchievements.class);
                if (userAchievements != null) {
                    callback.onCallback(userAchievements);
                } else {
                    callback.onCallback(new UserAchievements(userId)); // Return new instance if not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("UserAchievements", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public interface DataCallback {
        void onCallback(UserAchievements userAchievements);
    }

    // Inner class to track individual achievement progress
    public static class AchievementProgress {
        public String achievementId;
        public int currentValue; // Current progress
        public int totalValue; // Total required for completion

        public AchievementProgress() {
            // Default constructor required for calls to DataSnapshot.getValue(AchievementProgress.class)
        }

        public AchievementProgress(String achievementId, int currentValue, int totalValue) {
            this.achievementId = achievementId;
            this.currentValue = currentValue;
            this.totalValue = totalValue;
        }
    }
}