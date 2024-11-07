package com.example.fitness_social.tables;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

@IgnoreExtraProperties
public class UserInfo {
    public enum UserInfoStatus {
        OFFLINE,
        ONLINE,
        RUNNING,
        CYCLING,
        PUSHUPS,
        NOPHONE,
        CULTURE
}
    // Initialization required
    public String uid;              // After implementing the login function, FirebaseUser.getUid();
    public String user_name;        // After implementing the login function, FirebaseUser.getDisplayName();
    public String profile_url;      // After implementing the login function and Cloud Storage, FirebaseUser.getPhotoUrl();
    public String email_address;    // After implementing the login function, FirebaseUser.getEmail();

    // Unable to initialize automatically, must define values later
    public String phone_number;
    public int age;
    public int weight;
    public int height;

    // Automatic initialization values
    public int point;
    public int challenge_win_times;
    public UserInfoStatus status;
    public ArrayList<String> achievements_finish_list = new ArrayList<>();  // Link with Achievement.achievement_id
    public ArrayList<String> friends_list = new ArrayList<>();              // Link with UserInfo.user_info_id

    // Specific id
    public String user_info_id = "";

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public UserInfo(String uid, String user_name, String profile_url, String email_address) {
        this.uid = uid;
        this.user_name = user_name;
        this.profile_url = profile_url;
        this.email_address = email_address;

        this.point = 0;
        this.challenge_win_times = 0;
        this.status = UserInfoStatus.ONLINE;
        this.achievements_finish_list = new ArrayList<>();
        this.friends_list = new ArrayList<>();

        this.phone_number = "";
        this.age = -1;
        this.weight = -1;
        this.height = -1;
    }

    public static void insertAndUpdate(UserInfo userInfo) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(userInfo.user_info_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("user_info").push().getKey();
           userInfo.user_info_id = key;
        }
        else {
            // Update to database
            key = userInfo.user_info_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user_info/" + key,userInfo);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<UserInfo> allUserInfo);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userInfoRef = mDatabase.child("user_info");
        ArrayList<UserInfo> allUserInfo = new ArrayList<>();
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserInfo userInfo = childSnapshot.getValue(UserInfo.class);
                    allUserInfo.add(userInfo);
                }
                callback.onCallback(allUserInfo);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 处理数据库错误
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String user_info_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_info").child(user_info_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("UserInfo-deleteWithId", "Success or No such id");
            } else {
                Log.d("UserInfo-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        UserInfo sampleUserInfo1 = new UserInfo("account_1", "Andrew", "Andrew_profile_url", "Andrew@gmail.com");
        UserInfo sampleUserInfo2 = new UserInfo("account_2", "Dylan", "Dylan_profile_url", "Dylan@gmail.com");
        UserInfo sampleUserInfo3 = new UserInfo("account_3", "Howard", "Howard_profile_url", "Howard@gmail.com");
        UserInfo sampleUserInfo4 = new UserInfo("account_4", "Navi", "Navi_profile_url", "Navi@gmail.com");
        UserInfo sampleUserInfo5 = new UserInfo("account_5", "Tony", "Tony_profile_url", "Tony@gmail.com");

        DatabaseReference userInfoRef = mDatabase.child("user_info");
        userInfoRef.removeValue();

        UserInfo.insertAndUpdate(sampleUserInfo1);
        UserInfo.insertAndUpdate(sampleUserInfo2);
        UserInfo.insertAndUpdate(sampleUserInfo3);
        UserInfo.insertAndUpdate(sampleUserInfo4);
        UserInfo.insertAndUpdate(sampleUserInfo5);
    }
}



