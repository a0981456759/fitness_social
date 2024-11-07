package com.example.fitness_social.activities;

// TermsConditionsActivity.java
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.example.fitness_social.R;

public class TermsConditionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions); // Use your layout with ScrollView and TextView

        TextView termsTextView = findViewById(R.id.termsConditionsText); // Replace with your TextView ID
        String termsContent = getString(R.string.terms_conditions_content);
        termsTextView.setText(Html.fromHtml(termsContent, Html.FROM_HTML_MODE_LEGACY)); // For API 24 and above
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));


    }
    public void backButtonOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}