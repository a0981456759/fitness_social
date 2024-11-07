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
public class UserPosition {
    
    // Initialization required
    public String user_id;      // Link with UserInfo.user_info_id
    public String latitude;
    public String longitude;

    // Specific id
    public String user_position_id = "";

    public UserPosition() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public UserPosition(String user_id, String latitude, String longitude) {
        this.user_id = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static void insertAndUpdate(UserPosition userPosition) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(userPosition.user_position_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("user_position").push().getKey();
           userPosition.user_position_id = key;
        }
        else {
            // Update to database
            key = userPosition.user_position_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user_position/" + key,userPosition);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<UserPosition> allUserPosition);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userPositionRef = mDatabase.child("user_position");
        ArrayList<UserPosition> allUserPosition = new ArrayList<>();
        userPositionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserPosition userPosition = childSnapshot.getValue(UserPosition.class);
                    allUserPosition.add(userPosition);
                }
                callback.onCallback(allUserPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String user_position_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_position").child(user_position_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("UserPosition-deleteWithId", "Success or No such id");
            } else {
                Log.d("UserPosition-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        UserPosition sampleUserPosition1 = new UserPosition("-O7DI4G5PVpNEgMaHTVW", "37.422083", "-122.083917");
        UserPosition sampleUserPosition2 = new UserPosition("-O7DI4GAlSeltAuAi-bu", "38.422083", "-123.083917");
        UserPosition sampleUserPosition3 = new UserPosition("-O7DI4GAlSeltAuAi-bv", "39.422083", "-124.083917");
        UserPosition sampleUserPosition4 = new UserPosition("-O7DI4GAlSeltAuAi-bw", "30.422083", "-125.083917");
        UserPosition sampleUserPosition5 = new UserPosition("-O7DI4GB-1i550XsuZhG", "31.422083", "-126.083917");

        DatabaseReference userPositionRef = mDatabase.child("user_position");
        userPositionRef.removeValue();

        UserPosition.insertAndUpdate(sampleUserPosition1);
        UserPosition.insertAndUpdate(sampleUserPosition2);
        UserPosition.insertAndUpdate(sampleUserPosition3);
        UserPosition.insertAndUpdate(sampleUserPosition4);
        UserPosition.insertAndUpdate(sampleUserPosition5);
    }
}
