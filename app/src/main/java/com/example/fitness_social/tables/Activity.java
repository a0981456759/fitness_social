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
public class Activity {
    public enum Activity_Unit {
        KM,
        MIN,
        HOUR,
        TIME,
        //...
        // Add here if you want
    }

    // Initialization required
    public String activity_name;
    public String image_path;        // I suggest saving the activity image locally.
    public Activity_Unit unit;
    public int point_per_unit;
    public String rules;
    
    // Optional fields
    // String description

    // Specific id
    public String activity_id = "";

    public Activity() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Activity(String activity_name, String image_path, Activity_Unit unit, int point_per_unit, String rules) {
        this.activity_name = activity_name;
        this.image_path = image_path;
        this.unit = unit;
        this.point_per_unit = point_per_unit;
        this.rules = rules;
    }

    public static void insertAndUpdate(Activity activity) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(activity.activity_id.isEmpty()) {
            // Insert to database
            key = mDatabase.child("activity").push().getKey();
            activity.activity_id = key;
        }
        else {
            // Update to database
            key = activity.activity_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/activity/" + key,activity);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<Activity> allActivity);
    }

    public static void selectAll(Activity.DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference activityRef = mDatabase.child("activity");
        ArrayList<Activity> allActivity = new ArrayList<>();
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Activity activity = childSnapshot.getValue(Activity.class);
                    allActivity.add(activity);
                }
                callback.onCallback(allActivity);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String activity_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("activity").child(activity_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Activity-deleteWithId", "Success or No such id");
            } else {
                Log.d("Activity-deleteWithId", "Fail: " + task.getException());
            }
        });
    }
    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        Activity sampleActivity1 = new Activity("Run", "Run_url", Activity_Unit.KM, 1000,"Run_rules");
        Activity sampleActivity2 = new Activity("Cycling", "Cycling_url", Activity_Unit.KM, 500,"Cycling_rules");
        Activity sampleActivity3 = new Activity("Pushup", "Pushup_url", Activity_Unit.TIME, 100,"Pushup_rules");
        Activity sampleActivity4 = new Activity("No Phone Use", "No Phone Use_url", Activity_Unit.HOUR, 500,"No Phone Use_rules");
        Activity sampleActivity5 = new Activity("Culture Activity", "Culture Activity_url", Activity_Unit.HOUR, 1000,"Culture Activity_rules");

        DatabaseReference activityRef = mDatabase.child("activity");
        activityRef.removeValue();

        Activity.insertAndUpdate(sampleActivity1);
        Activity.insertAndUpdate(sampleActivity2);
        Activity.insertAndUpdate(sampleActivity3);
        Activity.insertAndUpdate(sampleActivity4);
        Activity.insertAndUpdate(sampleActivity5);
    }
}
