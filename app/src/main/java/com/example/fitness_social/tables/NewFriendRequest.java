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
public class NewFriendRequest {
    public enum NewFriendRequestStatus {
        SENT,
        ACCEPT,
        REJECT
        //...
        // Add here if you want
}

    // Initialization required
    public String requester_id;     // Link with UserInfo.user_info_id
    public String receiver_id;      // Link with UserInfo.user_info_id


    // Automatic initialization values
    public NewFriendRequestStatus status;

    // Optional fields
    // public String description;
    
    // Specific id
    public String new_friend_request_id = "";

    public NewFriendRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public NewFriendRequest(String requester_id, String receiver_id) {
        this.requester_id = requester_id;
        this.receiver_id = receiver_id;

        this.status = NewFriendRequestStatus.SENT;
    }

    public static void insertAndUpdate(NewFriendRequest newFriendRequest) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(newFriendRequest.new_friend_request_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("new_friend_request").push().getKey();
           newFriendRequest.new_friend_request_id = key;
        }
        else {
            // Update to database
            key = newFriendRequest.new_friend_request_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/new_friend_request/" + key,newFriendRequest);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<NewFriendRequest> allNewFriendRequest);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference newFriendRequestRef = mDatabase.child("new_friend_request");
        ArrayList<NewFriendRequest> allNewFriendRequest = new ArrayList<>();
        newFriendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    NewFriendRequest newFriendRequest = childSnapshot.getValue(NewFriendRequest.class);
                    allNewFriendRequest.add(newFriendRequest);
                }
                callback.onCallback(allNewFriendRequest);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String new_friend_request_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("new_friend_request").child(new_friend_request_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("NewFriendRequest-deleteWithId", "Success or No such id");
            } else {
                Log.d("NewFriendRequest-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        NewFriendRequest sampleNewFriendRequest1 = new NewFriendRequest("-O7DI4G5PVpNEgMaHTVW", "-O7DI4GAlSeltAuAi-bu");
        NewFriendRequest sampleNewFriendRequest2 = new NewFriendRequest("-O7DI4GB-1i550XsuZhG", "-O7DI4GAlSeltAuAi-bw");
        NewFriendRequest sampleNewFriendRequest3 = new NewFriendRequest("-O7DI4GAlSeltAuAi-bv", "-O7DI4GAlSeltAuAi-bu");

        DatabaseReference newFriendRequestRef = mDatabase.child("new_friend_request");
        newFriendRequestRef.removeValue();

        NewFriendRequest.insertAndUpdate(sampleNewFriendRequest1);
        NewFriendRequest.insertAndUpdate(sampleNewFriendRequest2);
        NewFriendRequest.insertAndUpdate(sampleNewFriendRequest3);
    }
}
