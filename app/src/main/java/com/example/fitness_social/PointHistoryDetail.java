package com.example.fitness_social;

import com.example.fitness_social.tables.UserDailyPlan;
import com.example.fitness_social.tables.UserPointHistory;

public class PointHistoryDetail{

    public UserDailyPlan userDailyPlan;
    public UserPointHistory userPointHistory;

    public PointHistoryDetail(UserDailyPlan userDailyPlan, UserPointHistory userPointHistory) {
        this.userDailyPlan = userDailyPlan;
        this.userPointHistory = userPointHistory;
    }

}