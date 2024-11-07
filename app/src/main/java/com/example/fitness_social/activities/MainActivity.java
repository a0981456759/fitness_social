package com.example.fitness_social.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.fitness_social.R;
import com.example.fitness_social.fragments.navigation_bar.HomeFragment;
import com.example.fitness_social.databinding.ActivityMainBinding;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * Navigate to login or home page depending on login status
 */
public class MainActivity extends AppCompatActivity {

    // Variables
    private AppBarConfiguration appBarConfiguration;
    private final HomeFragment homeFragment = new HomeFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_content_main
        );
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {}
        });
        authenticationDetection();
        getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, homeFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add items to action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle clicks on home button
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    /*
    Direct user to login page if not logged in and otherwise refresh Firebase authentication
     */
    private void authenticationDetection() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            reload();
        }
    }

    /*
    Refresh Firebase authentication
     */
    private void reload() {
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Main", "reload", task.getException());
            } else {
                Log.e("Main", "reload", task.getException());
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

}