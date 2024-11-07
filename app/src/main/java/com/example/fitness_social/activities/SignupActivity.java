package com.example.fitness_social.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.example.fitness_social.R;

import com.example.fitness_social.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    private FirebaseAuth mAuth;
    private Runnable emailVerificationChecker;
    private Handler handler = new Handler();
    private static final int CHECK_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        initPage();
        setAllOnClick();
    }

    private void initPage() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setAllOnClick() {
        setLoginTextOnClick();
        setSignupButtonOnClick();
        setResendTextOnClick();
    }

    private void setLoginTextOnClick() {
        // Add the "Login" text functionality
        String textLogin = binding.textViewLogin.getText().toString();
        SpannableString spannableStringLogin = new SpannableString(textLogin);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // Change to the color you prefer
                ds.setUnderlineText(false); // Disable underline if you don't want it
            }
        };
        spannableStringLogin.setSpan(clickableSpan, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.textViewLogin.setText(spannableStringLogin);
        binding.textViewLogin.setMovementMethod(LinkMovementMethod.getInstance()); // Make links clickable
        binding.textViewLogin.setHighlightColor(Color.TRANSPARENT); // Optional: removes the highlight effect when clicked
    }

    private void setSignupButtonOnClick() {
        // Handle signUpButton onClick
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.enterEmailEditText.getText().toString();
                String password = binding.enterPasswordEditText.getText().toString();
                createAccount(email, password);
            }
        });
    }

    private void setResendTextOnClick() {
        // Add the "Resend" text functionality
        String verifyEmailText = binding.verifyEmailText.getText().toString();
        SpannableString spannableString = new SpannableString(verifyEmailText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                sendEmailVerification();
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE); // Change to the color you prefer
                ds.setUnderlineText(false); // Disable underline if you don't want it
            }
        };
        spannableString.setSpan(clickableSpan, 78, 84, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.verifyEmailText.setText(spannableString);
        binding.verifyEmailText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.verifyEmailText.setHighlightColor(Color.TRANSPARENT);
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = binding.enterEmailEditText.getText().toString();
        String password = binding.enterPasswordEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            binding.enterEmailEditText.setError("Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.enterPasswordEditText.setError("Required.");
            valid = false;
        }
        if (!valid) {
            Toast.makeText(getApplicationContext(), "Please enter email and password!",
                    Toast.LENGTH_SHORT).show();
            return valid;
        }
        else {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                Toast.makeText(getApplicationContext(), "Invalid email address!",
                        Toast.LENGTH_SHORT).show();
                binding.enterEmailEditText.setError("Invalid.");
                return false;
            }
        }
        return valid;
    }

    // Request to Firebase to create new account
    private void createAccount(String email, String password) {
        Log.d("Signup", "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        binding.signUpButton.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("Signup", "createUserWithEmail:success");
                            verifyAccount();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Signup", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "This email address has already been registered or invalid!",
                                    Toast.LENGTH_SHORT).show();
                            binding.signUpButton.setEnabled(true);
                        }
                    }
                });
    }

    // verify email address
    private void verifyAccount() {
        binding.verifyEmailText.setVisibility(View.VISIBLE);
        sendEmailVerification();
        startEmailVerificationCheck();
    }

    private void sendEmailVerification() {
        // Disable button
        binding.signUpButton.setEnabled(false);
        // Send verification email
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Re-enable button
                        binding.signUpButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Signup", "sendEmailVerification", task.getException());
                            if (task.getException() instanceof FirebaseTooManyRequestsException) {
                                Toast.makeText(getApplicationContext(), "Too many requests. Please try again later.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    // Use Runnable to check email verification status
    private void startEmailVerificationCheck() {
        FirebaseUser user = mAuth.getCurrentUser();
        emailVerificationChecker = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (user.isEmailVerified()) {
                            Toast.makeText(getApplicationContext(),
                                    "Verification successful, You can log in now!",
                                    Toast.LENGTH_SHORT).show();
                            // Email verified, direct to login page
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            handler.removeCallbacks(emailVerificationChecker);
                        } else {
                            Log.d("RunableEmailVerificationCheck","not verify yet");
                            // Keep checking
                            handler.postDelayed(emailVerificationChecker, CHECK_INTERVAL);
                        }
                    }
                });
            }
        };
        handler.postDelayed(emailVerificationChecker, CHECK_INTERVAL);
    }

    // When exit this page, delete the unverified account
    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null && !user.isEmailVerified()) {
            Log.d("onpause","del");
            user.delete();
        }
        handler.removeCallbacks(emailVerificationChecker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null && !user.isEmailVerified()) {
            user.delete();
        }
        handler.removeCallbacks(emailVerificationChecker);
    }
}