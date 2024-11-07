package com.example.fitness_social.tables;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class UserPointHistory {

    // Initialization required
    public String daily_plan_id;        // Link with UserDailyPlan.user_daily_plan_id
    public int change;                  // Use Activity.point_per_unit and UserDailyPlan.current to calculate point-change
    public int time;

    // Automatic initialization values
    public String change_time;

    // Specific id
    public String user_point_history_id = "";

    public UserPointHistory() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public UserPointHistory(String daily_plan_id, int change, int time) {
        this.daily_plan_id = daily_plan_id;
        this.change = change;
        this.time = time;

        this.change_time = ZonedDateTime.now().toString();
    }

    public static void insertAndUpdate(UserPointHistory userPointHistory) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(userPointHistory.user_point_history_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("user_point_history").push().getKey();
           userPointHistory.user_point_history_id = key;
        }
        else {
            // Update to database
            key = userPointHistory.user_point_history_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user_point_history/" + key,userPointHistory);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<UserPointHistory> allUserPointHistory);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userPointHistoryRef = mDatabase.child("user_point_history");
        ArrayList<UserPointHistory> allUserPointHistory = new ArrayList<>();
        userPointHistoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserPointHistory userPointHistory = childSnapshot.getValue(UserPointHistory.class);
                    allUserPointHistory.add(userPointHistory);
                }
                callback.onCallback(allUserPointHistory);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 处理数据库错误
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String user_point_history_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_point_history").child(user_point_history_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("UserPointHistory-deleteWithId", "Success or No such id");
            } else {
                Log.d("UserPointHistory-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userPointHistoryRef = mDatabase.child("user_point_history");
        userPointHistoryRef.removeValue();
    }
}
