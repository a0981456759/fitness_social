package com.example.fitness_social.tables;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Achievement {
    
    // Initialization required
    public String achievement_name;
    public String image_path;        // I suggest saving the achievement image locally.
    public String description;

    // Specific id
    public String achievement_id = "";

    public Achievement() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Achievement(String achievement_name, String image_path, String description) {
        this.achievement_name = achievement_name;
        this.image_path = image_path;
        this.description = description;
    }

    public static void insertAndUpdate(Achievement achievement) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(achievement.achievement_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("achievement").push().getKey();
           achievement.achievement_id = key;
        }
        else {
            // Update to database
            key = achievement.achievement_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/achievement/" + key,achievement);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<Achievement> allAchievement);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference achievementRef = mDatabase.child("achievement");
        ArrayList<Achievement> allAchievement = new ArrayList<>();
        achievementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Achievement achievement = childSnapshot.getValue(Achievement.class);
                    allAchievement.add(achievement);
                }
                callback.onCallback(allAchievement);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String achievement_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("achievement").child(achievement_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Achievement-deleteWithId", "Success or No such id");
            } else {
                Log.d("Achievement-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Achievement sampleAchievement1 = new Achievement("Peashooter", "Peashooter_path", "Peashooter_des");
        Achievement sampleAchievement2 = new Achievement("Sunflower", "Sunflower_path", "Sunflower_des");
        Achievement sampleAchievement3 = new Achievement("Cherry Bomb", "Cherry Bomb_path", "Cherry Bomb_des");

        DatabaseReference achievementRef = mDatabase.child("achievement");
        achievementRef.removeValue();

        Achievement.insertAndUpdate(sampleAchievement1);
        Achievement.insertAndUpdate(sampleAchievement2);
        Achievement.insertAndUpdate(sampleAchievement3);
    }
}
