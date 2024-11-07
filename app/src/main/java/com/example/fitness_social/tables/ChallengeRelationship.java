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
public class ChallengeRelationship {
    public enum ChallengeRelationshipStatus {
        SENT,
        ACCEPT,
        REJECT,
        ONGOING,
        FINISH
        //...
        // Add here if you want
}

    // Initialization required
    public String requester_id;     // Link with UserInfo.user_info_id
    public String receiver_id;      // Link with UserInfo.user_info_id
    public String begin_time;       // I suggest that the default challenge time starts from the second day of Accept and lasts for 7 days, implement the function of selecting the time in future
    public String end_time;
    public String daily_task1_id;   // Link with Activity.activity_id
    public String daily_task2_id;   // Link with Activity.activity_id
    
    // Automatic initialization values
    public ChallengeRelationshipStatus status;
    public String create_time;
    public int requester_point;
    public int receiver_point;
    public String winner_id;

    // Specific id
    public String challenge_relationship_id = "";

    public ChallengeRelationship() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public ChallengeRelationship(String requester_id, String receiver_id, String begin_time, String end_time, String daily_task1_id, String daily_task2_id) {
        this.requester_id = requester_id;
        this.receiver_id = receiver_id; 
        this.begin_time = begin_time;
        this.end_time = end_time; 
        this.daily_task1_id = daily_task1_id;
        this.daily_task2_id = daily_task2_id;

        this.status = ChallengeRelationshipStatus.SENT;
        this.create_time = ZonedDateTime.now().toString();
        this.requester_point = 0;
        this.receiver_point = 0;
        this.winner_id = receiver_id;
        
    }

    public static void init() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        ChallengeRelationship sampleChallengeRelationship1 = new ChallengeRelationship("-O7DI4G5PVpNEgMaHTVW", "-O7DI4GAlSeltAuAi-bu", "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-27T17:38:57.801855+10:00[Australia/Sydney]", "-O7DI4GCQeCqjXYmd-46", "-O7DI4GDXUP4tVqHv278");
        ChallengeRelationship sampleChallengeRelationship2 = new ChallengeRelationship("-O7DI4GB-1i550XsuZhG", "-O7DI4GAlSeltAuAi-bw", "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-27T17:38:57.801855+10:00[Australia/Sydney]", "-O7DI4GCQeCqjXYmd-46", "-O7DI4GDXUP4tVqHv278");
        ChallengeRelationship sampleChallengeRelationship3 = new ChallengeRelationship("-O7DI4GAlSeltAuAi-bv", "-O7DI4GAlSeltAuAi-bu", "2024-09-20T17:38:57.801855+10:00[Australia/Sydney]",
                "2024-09-27T17:38:57.801855+10:00[Australia/Sydney]", "-O7DI4GCQeCqjXYmd-46", "-O7DI4GDXUP4tVqHv278");

        DatabaseReference challengeRelationshipRef = mDatabase.child("challenge_relationship");
        challengeRelationshipRef.removeValue();

        ChallengeRelationship.insertAndUpdate(sampleChallengeRelationship1);
        ChallengeRelationship.insertAndUpdate(sampleChallengeRelationship2);
        ChallengeRelationship.insertAndUpdate(sampleChallengeRelationship3);
    }
    public static void insertAndUpdate(ChallengeRelationship challengeRelationship) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key;
        if(challengeRelationship.challenge_relationship_id.isEmpty()) {
            // Insert to database
           key = mDatabase.child("challenge_relationship").push().getKey();
           challengeRelationship.challenge_relationship_id = key;
        }
        else {
            // Update to database
            key = challengeRelationship.challenge_relationship_id;
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/challenge_relationship/" + key,challengeRelationship);
        mDatabase.updateChildren(childUpdates);
    }

    public interface DataCallback {
        void onCallback(ArrayList<ChallengeRelationship> allChallengeRelationship);
    }

    public static void selectAll(DataCallback callback) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference challengeRelationshipRef = mDatabase.child("challenge_relationship");
        ArrayList<ChallengeRelationship> allChallengeRelationship = new ArrayList<>();
        challengeRelationshipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    ChallengeRelationship challengeRelationship = childSnapshot.getValue(ChallengeRelationship.class);
                    allChallengeRelationship.add(challengeRelationship);
                }
                callback.onCallback(allChallengeRelationship);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteById(String challenge_relationship_id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("challenge_relationship").child(challenge_relationship_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("ChallengeRelationship-deleteWithId", "Success or No such id");
            } else {
                Log.d("ChallengeRelationship-deleteWithId", "Fail: " + task.getException());
            }
        });
    }

}
