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
public class UserDailyPlan {
    public enum DailyPlanType {
        DAILY_PLAN,
        DAILY_CHALLENGES,
        CHALLENGE_TASK,
        CHALLENGE_BONUS
        //...
        // Add here if you want
}
    public enum DailyPlanStatus {
        FINISH,
        ONGOING,
        NOT_COMING,
        NOT_FINISH
        //...
        // Add here if you want
    }

    // Initialization required
    public String user_id;      // Link with UserInfo.user_info_id
    public String activity_name;  // Link with Activity.activity_id
    public DailyPlanType type;
    public String begin_time;
    public String end_time;
    public int requirement;
    public int streak;

    // Automatic initialization values
    public DailyPlanStatus status;
    public String create_time;
    public int current;     // The number of current complete

    // Specific id
    public String daily_plan_id = "";

    public UserDailyPlan() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public UserDailyPlan(String user_id, String activity_name, DailyPlanType type, String begin_time, String end_time, int requirement, int streak) {
        this.user_id = user_id;
        this.activity_name = activity_name;
        this.type = type;
        this.begin_time = begin_time;
        this.end_time = end_time;
        this.requirement = requirement;
        this.streak = streak;

        this.status = DailyPlanStatus.NOT_COMING;
        this.current = 0;
        this.create_time = ZonedDateTime.now().toString();
    }
    public UserDailyPlan(String user_id, DailyPlanType type) {
        this.user_id = user_id;
        this.activity_name = "";
        this.type = type;
        this.begin_time = ZonedDateTime.now().toString();
        this.end_time = ZonedDateTime.now().toString();
        this.requirement = -1;
        this.streak = -1;
        this.status = DailyPlanStatus.FINISH;
        this.current = -1;
        this.create_time = ZonedDateTime.now().toString();
    }

    public static void insertAndUpdate(UserDailyPlan userDailyPlan) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(userDailyPlan.daily_plan_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("user_daily_plan").push().getKey();
           userDailyPlan.daily_plan_id = key;
        }
        else {
            // Update to database
            key = userDailyPlan.daily_plan_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user_daily_plan/" + key,userDailyPlan);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<UserDailyPlan> allUserDailyPlan);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userDailyPlanRef = mDatabase.child("user_daily_plan");
        ArrayList<UserDailyPlan> allUserDailyPlan = new ArrayList<>();
        userDailyPlanRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserDailyPlan userDailyPlan = childSnapshot.getValue(UserDailyPlan.class);
                    allUserDailyPlan.add(userDailyPlan);
                }
                callback.onCallback(allUserDailyPlan);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 处理数据库错误
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String daily_plan_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_daily_plan").child(daily_plan_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("UserDailyPlan-deleteWithId", "Success or No such id");
            } else {
                Log.d("UserDailyPlan-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        UserDailyPlan sampleUserDailyPlan1 = new UserDailyPlan("-O7DI4G5PVpNEgMaHTVW", "Running", DailyPlanType.DAILY_PLAN, "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-20T23:59:59.801855+10:00[Australia/Sydney]", 3, 0);
        UserDailyPlan sampleUserDailyPlan2 = new UserDailyPlan("-O7DI4GAlSeltAuAi-bu", "Cycling", DailyPlanType.DAILY_CHALLENGES, "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-20T23:59:59.801855+10:00[Australia/Sydney]", 20, 1);
        UserDailyPlan sampleUserDailyPlan3 = new UserDailyPlan("-O7DI4GAlSeltAuAi-bv", "Push Ups", DailyPlanType.CHALLENGE_TASK, "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-20T23:59:59.801855+10:00[Australia/Sydney]", 5, 2);
        UserDailyPlan sampleUserDailyPlan4 = new UserDailyPlan("-O7DI4GAlSeltAuAi-bw",DailyPlanType.CHALLENGE_BONUS);

        DatabaseReference userDailyPlanRef = mDatabase.child("user_daily_plan");
        userDailyPlanRef.removeValue();

        UserDailyPlan.insertAndUpdate(sampleUserDailyPlan1);
        UserDailyPlan.insertAndUpdate(sampleUserDailyPlan2);
        UserDailyPlan.insertAndUpdate(sampleUserDailyPlan3);
        UserDailyPlan.insertAndUpdate(sampleUserDailyPlan4);
    }
}
