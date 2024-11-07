package com.example.fitness_social.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitness_social.R;
import com.example.fitness_social.tables.UserInfo;

import java.util.ArrayList;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private ArrayList<UserInfo> userList;
    private ArrayList<UserInfo> userListFull;

    public LeaderboardAdapter(ArrayList<UserInfo> userList) {
        this.userList = new ArrayList<>(userList);
        this.userListFull = new ArrayList<>(userList);
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
        holder.userPoints.setText(String.valueOf(userInfo.point));

        // Load the profile image using Glide
        Glide.with(holder.itemView.getContext())
                .load(userInfo.profile_url) // Ensure this URL is valid
                .placeholder(R.drawable.incomplete) // Placeholder while loading
                .error(R.drawable.incomplete) // Fallback if the image fails to load
                .circleCrop() // Optional, for circular images
                .into(holder.userProfileImage);
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
        ImageView userProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userPoints = itemView.findViewById(R.id.user_points);
            userProfileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}