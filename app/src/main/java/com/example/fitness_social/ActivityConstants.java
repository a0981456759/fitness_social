package com.example.fitness_social;

/**
 * Constants associated with all activities (running, cycling, push ups, no phone use, culture)
 */
public class ActivityConstants {

    // Points scales
    public static final int RUNNING_POINTS_SCALE = 4;
    public static final int CYCLING_POINTS_SCALE = 3;
    public static final int PUSHUPS_POINTS_SCALE = 5;
    public static final int NOPHONE_POINTS_SCALE = 1;
    public static final int CULTURE_POINTS_SCALE = 4;

    // Speed statuses
    public static final int SPEED_TOO_SLOW = 1;
    public static final int SPEED_TOO_FAST = 2;
    public static final int SPEED_JUST_RIGHT = 3;

    // Rules text
    public static final String RUNNING_RULES =
            "\nYour time and distance are only recorded when your speed is above 5 km/h and below 15 km/h\n";
    public static final String CYCLING_RULES =
            "\nYour time and distance are only recorded when your speed is above 10 km/h and below 50 km/h\n";
    public static final String PUSHUPS_RULES =
            "\nBefore starting, place your phone facing upwards on the ground underneath your face, and when doing push ups bring your face as close to the phone screen as possible\n";
    public static final String NOPHONE_RULES =
            "\nThe timer will continue until you stop it or leave the app (you are allowed to turn your screen off)\n";
    public static final String CULTURE_RULES =
            "\nAfter clicking Start near a \"cultural\" area, the timer will continue until you stop it or leave the area by more than 100 metres\n";

    // Running messages
    public static final String INVALID_RUNNING_NOTE1 =
            "\nTimer paused: You must be running at a speed no less than 5 km/h\n";
    public static final String INVALID_RUNNING_NOTE2 =
            "\nTimer paused: You must be running at a speed no more than 15 km/h\n";

    // Cycling messages
    public static final String INVALID_CYCLING_NOTE1 =
            "\nTimer paused: You must be cycling at a speed no less than 15 km/h\n";
    public static final String INVALID_CYCLING_NOTE2 =
            "\nTimer paused: You must be cycling at a speed no more than 40 km/h\n";

    // Push up messages
    public static final String INVALID_PUSHUPS_NOTE1 =
            "\nYou are too close to the ground, please make sure you have enough distance from the ground at the highest point!\n";
    public static final String INVALID_PUSHUPS_NOTE2 =
            "\nYou are doing it too fast, do it slower, and do each push-up properly!\n";
    public static final String INVALID_PUSHUPS_NOTE3 =
            "\nCome on! Keep working hard, don't give up, speed up!\n";

    // User info text
    public static final String DISTANCE_INFO = "\nDistance Travelled Today: %d m";
    public static final String PUSHUPS_INFO = "\nPush Ups Done Today: %d push ups";
    public static final String NOPHONE_INFO = "\nTime with Phone Blocked Today: %d min";
    public static final String CULTURE_INFO = "\nTime at Cultural Activity Today: %d min";
    public static final String SPEED_INFO = "Current speed: %.2f km/h\n";
    public static final String POINTS_INFO = "Points Earned Today: %d\n";

    // Units
    public static final String DISTANCE_UNITS = "%d m";
    public static final String TIME_UNITS = "%d min";
    public static final String PUSHUPS_UNITS = "%d push ups";
    public static final String POINTS_UNITS = " (%d pts)";

}