package com.example.fitness_social.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitness_social.R;
import com.example.fitness_social.tables.UserDailyPlan;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;


public class DailyPlanProgressAdapter extends RecyclerView.Adapter<DailyPlanProgressAdapter.ViewHolder> {
    private List<UserDailyPlan> dailyPlans;


    public DailyPlanProgressAdapter(List<UserDailyPlan> dailyPlans) {
        this.dailyPlans = dailyPlans;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_plan_progress, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDailyPlan plan = dailyPlans.get(position);

        holder.activityTypeText.setText(plan.activity_name);

        // set different progress display format according to activity type
        String progressText;
        switch (plan.activity_name) {
            case "Running":
            case "Cycling":
                progressText = String.format("%.1f/%.1f km", plan.current/1000.0, plan.requirement/1000.0);
                System.out.println("RUNNING OR CYCLING");
                break;
            case "Push Ups":
                progressText = String.format("%d/%d times", plan.current, plan.requirement);
                System.out.println("PUSH UPS");
                break;
            case "No Phone Use":
                progressText = String.format("%d/%d minutes", plan.current, plan.requirement);
                System.out.println("NO PHONE USE");
                break;
            case "Culture":
                progressText = String.format("%d/%d minutes", plan.current, plan.requirement);
                System.out.println("CULTURE");
                break;
            default:
                progressText = String.format("%d/%d minutes", plan.current/60, plan.requirement/60);
                System.out.println("DEFAULT");
                break;
        }
        holder.progressText.setText(progressText);

        // calculate progress percentage
        int progress = (int)((plan.current * 100.0) / plan.requirement);
        holder.progressBar.setProgress(Math.min(progress, 100));
    }


    @Override
    public int getItemCount() {
        return dailyPlans.size();
    }


    public void updateData(List<UserDailyPlan> newPlans) {
        this.dailyPlans = newPlans;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityTypeText;
        TextView progressText;
        LinearProgressIndicator progressBar;


        ViewHolder(View itemView) {
            super(itemView);
            activityTypeText = itemView.findViewById(R.id.activityTypeText);
            progressText = itemView.findViewById(R.id.progressText);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

