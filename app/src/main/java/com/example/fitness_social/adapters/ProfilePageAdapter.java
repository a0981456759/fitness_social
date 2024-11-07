package com.example.fitness_social.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fitness_social.R;

import java.util.ArrayList;

public class ProfilePageAdapter extends RecyclerView.Adapter<ProfilePageAdapter.PointViewHolder> {

    private ArrayList<String> pointList;
    private ArrayList<String> imageList;
    private Context context;

    public ProfilePageAdapter(Context context,ArrayList<String> PointList) {
        this.context = context;
        this.pointList = PointList;
    }

    public ProfilePageAdapter(Context context, ArrayList<String> PointList, ArrayList<String> imageList) {
        this.context = context;
        this.pointList = PointList;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        if (imageList != null) {
            view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.icon).setVisibility(View.GONE);
        }
        return new PointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        if (imageList != null) {
            holder.PointTextView2.setText(pointList.get(position));
            int resID = context.getResources().getIdentifier(imageList.get(position), "drawable", context.getPackageName());
            holder.PointIcon.setImageResource(resID);
        }
        else {
            holder.PointTextView1.setText(pointList.get(position).substring(0,pointList.get(position).indexOf("\n")));
            holder.PointTextView2.setText(pointList.get(position).substring(pointList.get(position).indexOf("\n") + 1));
        }
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView PointTextView1;
        TextView PointTextView2;
        ImageView PointIcon;

        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            PointTextView1 = itemView.findViewById(R.id.text1);
            PointTextView2 = itemView.findViewById(R.id.text2);
            PointIcon = itemView.findViewById(R.id.icon);
        }
    }
}
