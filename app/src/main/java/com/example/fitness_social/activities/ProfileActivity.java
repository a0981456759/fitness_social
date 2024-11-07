package com.example.fitness_social.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitness_social.ActivityConstants;
import com.example.fitness_social.PointHistoryDetail;
import com.example.fitness_social.R;
import com.example.fitness_social.adapters.ProfilePageAdapter;
import com.example.fitness_social.databinding.ActivityProfileBinding;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.fragments.navigation_bar.PlanFragment;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;
import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserInfo;
import com.example.fitness_social.tables.UserPointHistory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private UserInfo userInfo = null;
    private final HomeFragment homeFragment = new HomeFragment();
    private final PlanFragment planFragment = new PlanFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private static ArrayList<String> pointList = new ArrayList<>();
    private static ArrayList<String> achievementList = new ArrayList<>();
    private static ArrayList<String> imageList = new ArrayList<>();
    private static HashMap<String, UserDailyPlan> dailyPlanMap = new HashMap<>();
    private static ArrayList<PointHistoryDetail> pointHistoryDetailList = new ArrayList<>();
    private Button showPointsButton;
    private Button showAchievementButton;
    private ImageButton backButton;
    private ProfileActivity profileActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.profilePage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        String uid = getIntent().getStringExtra("uid");
        dailyPlanMap.clear();
        pointHistoryDetailList.clear();
        UserDailyPlan.selectAll(allUserDailyPlan -> {
            for (int i = 0; i < allUserDailyPlan.size(); i++) {
                if (Objects.equals(allUserDailyPlan.get(i).user_id, uid)) {
                    dailyPlanMap.put(allUserDailyPlan.get(i).daily_plan_id, allUserDailyPlan.get(i));
                    // Log.d(" ",allUserDailyPlan.get(i).daily_plan_id);
                }
            }
            UserPointHistory.selectAll(allUserPointHistory -> {
                int flag = 0;
                for (int i = 0; i < allUserPointHistory.size(); i++) {
                    if (dailyPlanMap.containsKey(allUserPointHistory.get(i).daily_plan_id)) {
                        pointHistoryDetailList.add(new PointHistoryDetail(dailyPlanMap.get(allUserPointHistory.get(i).daily_plan_id), allUserPointHistory.get(i)));
                        // Log.d("Find history point ", allUserPointHistory.get(i).user_point_history_id);
                    }
                }
                pointHistoryDetailList.sort(Comparator.comparing(o -> o.userPointHistory.change_time));
                Collections.reverse(pointHistoryDetailList);
                setNavBar();
                initPage();
            });
        });
    }

    private void setAllOnClick() {
        binding.historyButton.setOnClickListener(view -> showPointDialog());
        binding.achievementsButton.setOnClickListener(view -> showAchievementDialog());
        binding.backButton.setOnClickListener(view -> finish());
    }

    private void showPointDialog() {
        pointHistoryDetailList.sort(Comparator.comparing(o -> o.userPointHistory.change_time));
        Collections.reverse(pointHistoryDetailList);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (int i = 0; i < pointHistoryDetailList.size(); i++) {
            PointHistoryDetail eachPHD = pointHistoryDetailList.get(i);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(eachPHD.userPointHistory.change_time);
            String pointHistory = "" + zonedDateTime.toLocalDate() + " " + zonedDateTime.toLocalTime().format(timeFormatter) + "\n"
                    + eachPHD.userDailyPlan.activity_name + "\nDuration:  " + convertSecondsToHHMMSS(eachPHD.userPointHistory.time) + "\n";
            switch (eachPHD.userDailyPlan.activity_name){
                case "Running":
                    pointHistory += eachPHD.userPointHistory.change / ActivityConstants.RUNNING_POINTS_SCALE + " meters   ";
                    break;
                case "Cycling":
                    pointHistory += eachPHD.userPointHistory.change / ActivityConstants.CYCLING_POINTS_SCALE + " meters   ";
                    break;
                case "Push Ups":
                    pointHistory += eachPHD.userPointHistory.change / ActivityConstants.PUSHUPS_POINTS_SCALE + " times   ";
                    break;
                case "No Phone Use":
                    pointHistory += eachPHD.userPointHistory.change / ActivityConstants.NOPHONE_POINTS_SCALE + " minutes   ";
                    break;
                case "Culture":
                    pointHistory += eachPHD.userPointHistory.change / ActivityConstants.CULTURE_POINTS_SCALE + " minutes   ";
                    break;

            }
            if (eachPHD.userPointHistory.change > 0)
                pointHistory += "+";
            pointHistory += eachPHD.userPointHistory.change;
            pointList.add(pointHistory);
        }

        Dialog dialog = new Dialog(profileActivity);
        dialog.setContentView(R.layout.adapter_profile);

        TextView title_text = dialog.findViewById(R.id.title_text);
        title_text.setText("Point history");
        RecyclerView recyclerView = dialog.findViewById(R.id.profile_page_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(profileActivity));
        ProfilePageAdapter adapter = new ProfilePageAdapter(profileActivity, pointList);
        recyclerView.setAdapter(adapter);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(800, 1200);
        }

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.divider_black));
        recyclerView.addItemDecoration(dividerItemDecoration);
        ImageView closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static String convertSecondsToHHMMSS(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void showAchievementDialog() {

        achievementList = userInfo.achievements_finish_list;
        if (achievementList == null)
            return;
        for (int i = 0; i<achievementList.size(); i++){
            switch (achievementList.get(i)){
                case "Culture Curator":
                    imageList.add("popular");
                    break;
                case "Locked In":
                    imageList.add("challenger");
                    break;
                case "Cycling Conqueror":
                    imageList.add("cycling2");
                    break;
                case "PathFinder":
                    imageList.add("pathfinder");
                    break;
                case "Push-up Professional":
                    imageList.add("strength");
                    break;
            }
        }

        // Modify here when achievement page finish

        Dialog dialog = new Dialog(profileActivity);
        dialog.setContentView(R.layout.adapter_profile);

        RecyclerView recyclerView = dialog.findViewById(R.id.profile_page_recycler_view);
        TextView title_text = dialog.findViewById(R.id.title_text);
        title_text.setText("Achievements");
        recyclerView.setLayoutManager(new LinearLayoutManager(profileActivity));
        ProfilePageAdapter adapter = new ProfilePageAdapter(profileActivity, achievementList, imageList);
        recyclerView.setAdapter(adapter);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(800, 1200);
        }

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);

        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_black));
        recyclerView.addItemDecoration(dividerItemDecoration);
        ImageView closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setNavBar() {
        binding.navigationBar.setSelectedItemId(R.id.friends);
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.profileContainer, homeFragment
                ).commit();
            } else if (item.getItemId() == R.id.plan) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.profileContainer, planFragment
                ).commitNow();
            } else if (item.getItemId() == R.id.achievements) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.profileContainer, achievementsFragment
                ).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.profileContainer, settingsFragment
                ).commit();
            }
            return true;
        });
    }

    private void initPage() {
        String uid = getIntent().getStringExtra("uid");
        profileActivity = this;

        UserInfo.selectAll(allUserInfo -> {
            for (int i = 0; i < allUserInfo.size(); i++) {
                if (Objects.equals(allUserInfo.get(i).uid, uid)) {
                    userInfo = allUserInfo.get(i);
                    break;
                }
            }

            binding.username.setText(userInfo.user_name);

            String imageUrl = userInfo.profile_url;
            if (!imageUrl.isEmpty()) {
                Glide.with(profileActivity)
                        .load(imageUrl)
                        .into(binding.profileImage);
            }
            Log.d("point", userInfo.point + " pts");
            binding.points.setText(userInfo.point + " pts");

            binding.activityType.setText(userInfo.status.toString());
            setAllOnClick();
        });

        generateChart();
    }

    private void generateChart() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(76, 175, 80));   // #4CAF50
        colors.add(Color.rgb(14, 100, 210));  // #0e64d2
        colors.add(Color.rgb(244, 67, 54));   // #F44336
        colors.add(Color.rgb(233, 192, 12));  // #e9c00c
        colors.add(Color.rgb(193, 43, 181));  // #c12bb5
        colors.add(Color.rgb(43, 193, 183));  // #2bc1b7
        colors.add(Color.rgb(193, 91, 43));   // #c15b2b

        BarChart barChart = findViewById(R.id.points_gained_chart);
        ArrayList<Integer> values = new ArrayList<>();
        values.add(0);
        values.add(0);
        values.add(0);
        values.add(0);
        values.add(0);
        values.add(0);
        values.add(0);

        for (int i = 0; i<pointHistoryDetailList.size(); i++){
            int dayFromToday = getDaysFromToday(ZonedDateTime.parse(pointHistoryDetailList.get(i).userPointHistory.change_time));
            // Log.d("values change", dayFromToday+"");
            if (dayFromToday == -1)
                continue;
            else{
                values.set(dayFromToday, values.get(dayFromToday) + pointHistoryDetailList.get(i).userPointHistory.change);
                // Log.d("values change", dayFromToday+ " " +pointHistoryDetailList.get(i).userPointHistory.change);
            }
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(values.size() - 1 - i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        YAxis rightYAxis = barChart.getAxisRight();
        rightYAxis.setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLast7Days()));

        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinimum(0f);

        barChart.invalidate();


        PieChart pieChart = findViewById(R.id.points_breakdown_chart);

        HashMap<String, Integer> dataMap = new HashMap<>();
        dataMap.put("Running", 0);
        dataMap.put("Cycling", 0);
        dataMap.put("Push Ups", 0);
        dataMap.put("No Phone Use", 0);
        dataMap.put("Culture", 0);

        for (int i = 0; i<pointHistoryDetailList.size(); i++){
            if (dataMap.containsKey(pointHistoryDetailList.get(i).userDailyPlan.activity_name)){
                dataMap.put(pointHistoryDetailList.get(i).userDailyPlan.activity_name, dataMap.get(pointHistoryDetailList.get(i).userDailyPlan.activity_name)
                        + pointHistoryDetailList.get(i).userPointHistory.change);
            }
        }

        ArrayList<PieEntry> entries1 = new ArrayList<>();
        entries1.add(new PieEntry(dataMap.get("Running"), "Running"));
        entries1.add(new PieEntry(dataMap.get("Cycling"), "Cycling"));
        entries1.add(new PieEntry(dataMap.get("Push Ups"), "Push Ups"));
        entries1.add(new PieEntry(dataMap.get("No Phone Use"), "No Phone Use"));
        entries1.add(new PieEntry(dataMap.get("Culture"), "Culture"));

        pieChart.getDescription().setEnabled(false);

        PieDataSet dataSet1 = new PieDataSet(entries1, "Activities");
        dataSet1.setColors(colors);

        PieData pieData = new PieData(dataSet1);
        pieChart.setData(pieData);

        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        for (LegendEntry entry : legend.getEntries()) {
            String label = entry.label;
            int color = entry.formColor;
            String hexColor = String.format("#%06X", (0xFFFFFF & color));
            // Log.d("LegendColor", label + ": " + hexColor);
        }
        legend.setEnabled(false);

        pieChart.invalidate();
    }

    public static int getDaysFromToday(ZonedDateTime dateTime) {
        LocalDate today = LocalDate.now();
        LocalDate givenDate = dateTime.toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(givenDate, today);
        if (daysBetween >= 0 && daysBetween < 7) {
            return (int) daysBetween;
        } else {
            return -1;
        }
    }

    public static String[] getLast7Days() {
        String[] days = new String[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate pastDay = today.minusDays(6-i);
            days[i] = String.valueOf(pastDay.getDayOfMonth());
        }
        return days;
    }

}