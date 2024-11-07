package com.example.fitness_social.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitness_social.R;
import com.example.fitness_social.tables.UserDailyPlan;
import java.util.List;


public class DailyPlanAdapter extends RecyclerView.Adapter<DailyPlanAdapter.ViewHolder> {
    private List<UserDailyPlan> plans;


    public DailyPlanAdapter(List<UserDailyPlan> plans) {
        this.plans = plans;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_plan, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDailyPlan plan = plans.get(position);
        holder.activityTypeText.setText(plan.activity_name);

        String progressText = "";
        switch (plan.activity_name) {
            case "Running":
            case "Cycling":
                progressText = String.format("Progress: %.1f/%.1f km",
                        plan.current/1000.0, plan.requirement/1000.0);
                break;
            case "Push Ups":
                progressText = String.format("Progress: %d/%d reps",
                        plan.current, plan.requirement);
                break;
            case "No Phone Use":
            case "Culture":
                progressText = String.format("Progress: %d/%d minutes",
                        plan.current, plan.requirement);
                break;
        }
        holder.progressText.setText(progressText);
    }


    @Override
    public int getItemCount() {
        return plans.size();
    }


    public void updatePlans(List<UserDailyPlan> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityTypeText;
        TextView progressText;


        ViewHolder(View itemView) {
            super(itemView);
            activityTypeText = itemView.findViewById(R.id.activityTypeText);
            progressText = itemView.findViewById(R.id.progressText);
        }
    }
}

