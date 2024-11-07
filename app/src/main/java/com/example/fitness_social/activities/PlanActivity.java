package com.example.fitness_social.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.DatePickerDialog;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import java.time.ZoneId;


import com.example.fitness_social.R;
import com.example.fitness_social.adapters.DailyPlanAdapter;
import com.example.fitness_social.databinding.ActivityPlanBinding;
import com.example.fitness_social.fragments.MyProfileFragment;
import com.example.fitness_social.fragments.navigation_bar.AchievementsFragment;
//import com.example.fitness_social.fragments.navigation_bar.FriendsFragment;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.fragments.navigation_bar.LeaderboardFragment;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;


import com.example.fitness_social.tables.UserDailyPlan;
import com.google.firebase.auth.FirebaseAuth;


public class PlanActivity extends AppCompatActivity {
    private DailyPlanAdapter dailyPlanAdapter;
    private List<UserDailyPlan> todayPlans = new ArrayList<>();
    private final LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
    private final AchievementsFragment achievementsFragment = new AchievementsFragment();
    private final HomeFragment homeFragment = new HomeFragment();
    private final MyProfileFragment myProfileFragment = new MyProfileFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private ActivityPlanBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.planPage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));


        setNavBar();
        setupActivityTypeSpinner();
        setupDatePickers();
        setupSaveButton();
        setupTodayPlans();
    }


    @Override
    protected void onStart() {
        super.onStart();
        binding.navigationBar.setSelectedItemId(R.id.plan);
    }


    private void setupTodayPlans() {
        binding.todayPlansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dailyPlanAdapter = new DailyPlanAdapter(todayPlans);
        binding.todayPlansRecyclerView.setAdapter(dailyPlanAdapter);

        loadTodayPlans();
    }


    private void loadTodayPlans() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // get today's date (system timezone)
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        String today = now.toLocalDate().toString();
        UserDailyPlan.selectAll(allPlans -> {
            todayPlans.clear();

            for (UserDailyPlan plan : allPlans) {
                //  extract date part from begin_time for comparison
                String planDate = plan.begin_time.split("T")[0]; // split to get date part

                if (plan.user_id.equals(userId) && planDate.equals(today) && plan.requirement != -1) {
                    todayPlans.add(plan);
                }
            }

            runOnUiThread(() -> dailyPlanAdapter.updatePlans(todayPlans));
        });
    }


    private void setNavBar(){
        binding.navigationBar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.friends) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.planContainer, leaderboardFragment
                ).commit();
            } else if (item.getItemId() == R.id.home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (item.getItemId() == R.id.achievements) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.planContainer, achievementsFragment
                ).commit();
            } else if (item.getItemId() == R.id.settings) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.planContainer, settingsFragment
                ).commit();
            }
            return true;
        });
    }


    private void setupActivityTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.activity_types,
                android.R.layout.simple_dropdown_item_1line
        );
        binding.activityTypeSpinner.setAdapter(adapter);


        binding.activityTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedActivity = parent.getItemAtPosition(position).toString();
            // Show or hide the corresponding input field based on the selection
            binding.runningGoalLayout.setVisibility(
                    selectedActivity.equals("Running") ? View.VISIBLE : View.GONE
            );
            binding.cyclingGoalLayout.setVisibility(
                    selectedActivity.equals("Cycling") ? View.VISIBLE : View.GONE
            );
            binding.pushupsGoalLayout.setVisibility(
                    selectedActivity.equals("Push Ups") ? View.VISIBLE : View.GONE
            );
            binding.noPhoneUseLayout.setVisibility(
                    selectedActivity.equals("No Phone Use") ? View.VISIBLE : View.GONE
            );
            binding.museumVisitLayout.setVisibility(
                    selectedActivity.equals("Culture Activity") ? View.VISIBLE : View.GONE
            );
        });
    }


    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        // Save the selected date
        final Calendar startDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();


        // Set up the start date picker
        binding.startDateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        startDate.set(selectedYear, selectedMonth, selectedDay);
                        String dateString = String.format(Locale.getDefault(),
                                "%d/%d/%d", selectedYear, selectedMonth + 1, selectedDay);
                        binding.startDateEditText.setText(dateString);


                        // Reset the end date if it is earlier than the start date
                        if (endDate.before(startDate)) {
                            binding.endDateEditText.setText("");
                        }
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });


        // Set up the end date picker
        binding.endDateEditText.setOnClickListener(v -> {
            if (binding.startDateEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please select start date first", Toast.LENGTH_SHORT).show();
                return;
            }


            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedEndDate = Calendar.getInstance();
                        selectedEndDate.set(selectedYear, selectedMonth, selectedDay);


                        // Check if the end date is after the start date
                        if (selectedEndDate.before(startDate)) {
                            Toast.makeText(this,
                                    "End date cannot be earlier than start date",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }


                        endDate.set(selectedYear, selectedMonth, selectedDay);
                        String dateString = String.format(Locale.getDefault(),
                                "%d/%d/%d", selectedYear, selectedMonth + 1, selectedDay);
                        binding.endDateEditText.setText(dateString);
                    },
                    year, month, day
            );


            // Set the minimum date to the start date
            datePickerDialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }


    private List<Calendar> calculateDateRange(String startDateStr, String endDateStr) {
        List<Calendar> dateRange = new ArrayList<>();


        // Parse the date strings (format: "yyyy/MM/dd")
        String[] startParts = startDateStr.split("/");
        String[] endParts = endDateStr.split("/");


        Calendar startDate = Calendar.getInstance();
        startDate.set(
                Integer.parseInt(startParts[0]), // year
                Integer.parseInt(startParts[1]) - 1, // month (0-based)
                Integer.parseInt(startParts[2]) // day
        );


        Calendar endDate = Calendar.getInstance();
        endDate.set(
                Integer.parseInt(endParts[0]), // year
                Integer.parseInt(endParts[1]) - 1, // month (0-based)
                Integer.parseInt(endParts[2]) // day
        );


        // Add each date in the range to the list
        Calendar currentDate = (Calendar) startDate.clone();
        while (!currentDate.after(endDate)) {
            dateRange.add((Calendar) currentDate.clone());
            currentDate.add(Calendar.DATE, 1);
        }


        return dateRange;
    }


    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            // validate inputs
            if (!validateInputs()) {
                return;
            }


            // get selected activity type and requirement value
            String selectedActivityType = binding.activityTypeSpinner.getText().toString();
            final String activityType = selectedActivityType.equals("Culture Activity") ?
                    "Culture" : selectedActivityType;
            double requirement = getRequirementValue(activityType);


            // get start and end dates
            String startDateStr = binding.startDateEditText.getText().toString();
            String endDateStr = binding.endDateEditText.getText().toString();


            // calculate all dates in the date range
            List<Calendar> dateRange = calculateDateRange(startDateStr, endDateStr);

            // check and update plans for each date
            UserDailyPlan.selectAll(allUserDailyPlan -> {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (Calendar date : dateRange) {
                    ZonedDateTime zonedDate = ZonedDateTime.ofInstant(
                            date.toInstant(),
                            ZoneId.systemDefault()
                    ).withHour(0).withMinute(0).withSecond(0);

                    // check if plan exists
                    boolean planExists = false;
                    UserDailyPlan existingPlan = null;

                    for (UserDailyPlan plan : allUserDailyPlan) {
                        if (userId.equals(plan.user_id) &&
                                activityType.equals(plan.activity_name) &&
                                plan.type.equals(UserDailyPlan.DailyPlanType.DAILY_PLAN) &&
                                ZonedDateTime.parse(plan.begin_time).toLocalDate()
                                        .equals(zonedDate.toLocalDate())) {
                            planExists = true;
                            existingPlan = plan;
                            break;
                        }
                    }


                    if (planExists && existingPlan != null) {
                        // update existing plan
                        if(existingPlan.activity_name.equals("Culture")||existingPlan.activity_name.equals("No Phone Use")){
                            existingPlan.requirement = ((int)requirement*60);
                        }
                        else {
                            existingPlan.requirement = (int) requirement;
                        }
                        UserDailyPlan.insertAndUpdate(existingPlan);
                    } else {
                        // create new plan
                        String beginTime = zonedDate.toString();
                        String endTime = zonedDate.withHour(23).withMinute(59).withSecond(59).toString();
                        UserDailyPlan newPlan = null;
                        if(activityType.equals("Culture")||activityType.equals("No Phone Use")){
                            newPlan = new UserDailyPlan(
                                    userId,
                                    activityType,
                                    UserDailyPlan.DailyPlanType.DAILY_PLAN,
                                    beginTime,
                                    endTime,
                                    (int) requirement * 60,
                                    0
                            );
                        }
                        else {
                            newPlan = new UserDailyPlan(
                                    userId,
                                    activityType,
                                    UserDailyPlan.DailyPlanType.DAILY_PLAN,
                                    beginTime,
                                    endTime,
                                    (int) requirement,
                                    0
                            );
                        }

                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);


                        if (date.equals(today)) {
                            newPlan.status = UserDailyPlan.DailyPlanStatus.ONGOING;
                        } else {
                            newPlan.status = UserDailyPlan.DailyPlanStatus.NOT_COMING;
                        }


                        UserDailyPlan.insertAndUpdate(newPlan);
                    }
                }


                runOnUiThread(() -> {
                    Toast.makeText(this, "Plan saved successfully!", Toast.LENGTH_SHORT).show();
                    resetInputFields();
                    loadTodayPlans();
                });
            });
        });
    }


    private void resetInputFields() {
        // Clear the activity type selection
        binding.activityTypeSpinner.setText("", false);


        // Hide all goal input fields
        binding.runningGoalLayout.setVisibility(View.GONE);
        binding.cyclingGoalLayout.setVisibility(View.GONE);
        binding.pushupsGoalLayout.setVisibility(View.GONE);
        binding.noPhoneUseLayout.setVisibility(View.GONE);
        binding.museumVisitLayout.setVisibility(View.GONE);


        // Clear all goal input values
        binding.runningGoalEditText.setText("");
        binding.cyclingGoalEditText.setText("");
        binding.pushupsGoalEditText.setText("");
        binding.noPhoneUseEditText.setText("");
        binding.museumVisitEditText.setText("");


        // Clear the date selection
        binding.startDateEditText.setText("");
        binding.endDateEditText.setText("");
    }


    private boolean validateInputs() {
        if (binding.activityTypeSpinner.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select an activity", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (binding.startDateEditText.getText().toString().isEmpty() ||
                binding.endDateEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return false;
        }


        // Validate the requirement value
        String activityType = binding.activityTypeSpinner.getText().toString();
        if (!validateRequirement(activityType)) {
            return false;
        }


        return true;
    }


    private double getRequirementValue(String activityType) {
        switch (activityType) {
            case "Running":
                return Double.parseDouble(binding.runningGoalEditText.getText().toString()) * 1000;
            case "Cycling":
                return Double.parseDouble(binding.cyclingGoalEditText.getText().toString()) * 1000;
            case "Push Ups":
                return Double.parseDouble(binding.pushupsGoalEditText.getText().toString());
            case "No Phone Use":
                return Double.parseDouble(binding.noPhoneUseEditText.getText().toString());
            case "Culture":
                return Double.parseDouble(binding.museumVisitEditText.getText().toString());
            default:
                return 0;
        }
    }


    private boolean validateRequirement(String activityType) {
        try {
            switch (activityType) {
                case "Running":
                    String runningInput = binding.runningGoalEditText.getText().toString();
                    if (runningInput.isEmpty()) {
                        Toast.makeText(this, "Please enter running distance goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!runningInput.matches("^[0-9]*(\\.[0-9]*)?$")) {
                        Toast.makeText(this, "Please enter a valid number for running distance", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    double runningGoal = Double.parseDouble(binding.runningGoalEditText.getText().toString());
                    if (runningGoal <= 0) {
                        Toast.makeText(this, "Running distance must be greater than 0", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;


                case "Cycling":
                    String cyclingInput = binding.cyclingGoalEditText.getText().toString();
                    if (cyclingInput.isEmpty()) {
                        Toast.makeText(this, "Please enter cycling distance goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!cyclingInput.matches("^[0-9]*(\\.[0-9]*)?$")) {
                        Toast.makeText(this, "Please enter a valid number for cycling distance", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    double cyclingGoal = Double.parseDouble(binding.cyclingGoalEditText.getText().toString());
                    if (cyclingGoal <= 0) {
                        Toast.makeText(this, "Cycling distance must be greater than 0", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
                case "Push Ups":
                    String pushupInput = binding.pushupsGoalEditText.getText().toString();
                    if (pushupInput.isEmpty()) {
                        Toast.makeText(this, "Please enter push ups goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!pushupInput.matches("^[0-9]*(\\.[0-9]*)?$")) {
                        Toast.makeText(this, "Please enter a valid number for push ups goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    double pushupGoal = Double.parseDouble(binding.pushupsGoalEditText.getText().toString());
                    if (pushupGoal <= 0) {
                        Toast.makeText(this, "Push up goal must be greater than 0", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;


                case "No Phone Use":
                    String noPhoneUseInput = binding.noPhoneUseEditText.getText().toString();
                    if (noPhoneUseInput.isEmpty()) {
                        Toast.makeText(this, "Please enter no phone use time goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!noPhoneUseInput.matches("^[0-9]*(\\.[0-9]*)?$")) {
                        Toast.makeText(this, "Please enter a valid number for no phone use time", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    double noPhoneUseGoal = Double.parseDouble(binding.noPhoneUseEditText.getText().toString());
                    if (noPhoneUseGoal <= 0) {
                        Toast.makeText(this, "No phone use time must be greater than 0", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (noPhoneUseGoal > 24) {
                        Toast.makeText(this, "No phone use time must be less than 24 hours", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;


                case "Culture Activity":
                    String museumVisitInput = binding.museumVisitEditText.getText().toString();
                    if (museumVisitInput.isEmpty()) {
                        Toast.makeText(this, "Please enter museum visit goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!museumVisitInput.matches("^[0-9]*(\\.[0-9]*)?$")) {
                        Toast.makeText(this, "Please enter a valid number for museum visit goal", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    double museumVisitGoal = Double.parseDouble(binding.museumVisitEditText.getText().toString());
                    if (museumVisitGoal <= 0) {
                        Toast.makeText(this, "Culture activity goal must be greater than 0", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (museumVisitGoal > 24) {
                        Toast.makeText(this, "Culture activity goal must be less than 24 hours", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    break;
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}