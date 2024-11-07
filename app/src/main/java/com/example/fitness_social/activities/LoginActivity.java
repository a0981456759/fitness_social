package com.example.fitness_social.activities;

import android.app.PendingIntent;
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
import android.widget.Toast;

import com.example.fitness_social.R;
import com.example.fitness_social.databinding.ActivityLoginBinding;
import com.example.fitness_social.fragments.ForgetPasswordFragment;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Log into app using account or Google, reset password and navigate to signup page
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // Regexes
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]+$";

    // Launchers and fragments
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncherWithGoogle = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> handleLoginResultWithGoogle(result.getData())
    );
    private final ForgetPasswordFragment forgetPasswordFragment = new ForgetPasswordFragment();

    // User client data
    private FirebaseAuth mAuth;
    private SignInClient signInClientWithGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginPage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        mAuth = FirebaseAuth.getInstance();
        bindButtons();
    }

    /*
    Bind UI buttons to functionality
     */
    private void bindButtons() {
        bindLoginButton();
        binding.loginWithGoogle.setOnClickListener(v -> loginWithGoogle());
        bindSignupButton();
        bindForgotPasswordButton();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
            }
        });
    }

    /*
    Bind UI login button to functionality
     */
    private void bindLoginButton() {
        binding.loginButton.setOnClickListener(v -> login(
                binding.enterEmailEditText.getText().toString(),
                binding.enterPasswordEditText.getText().toString()
        ));
        signInClientWithGoogle = Identity.getSignInClient(getApplicationContext());
    }

    /*
    Bind UI signup button to functionality
     */
    private void bindSignupButton() {
        SpannableString spannableStringSignUp = new SpannableString(binding.textViewSignUp.getText().toString());
        ClickableSpan clickableSpanSignUp = new ClickableSpan() {
            // Set functionality
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
            // Set appearance
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        spannableStringSignUp.setSpan(clickableSpanSignUp, 23, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.textViewSignUp.setText(spannableStringSignUp);
        binding.textViewSignUp.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textViewSignUp.setHighlightColor(Color.TRANSPARENT);
    }

    /*
    Bind UI forgot password button to functionality
     */
    private void bindForgotPasswordButton() {
        SpannableString spannableStringForgetPassword = new SpannableString(binding.textViewForgetPassword.getText().toString());
        ClickableSpan clickableSpanForgetPassword = new ClickableSpan() {
            // Set functionality
            @Override
            public void onClick(@NonNull View widget) {
                getSupportFragmentManager().beginTransaction().replace(R.id.forgetPasswordFragmentContainer, forgetPasswordFragment).commit();
            }
            // Set appearance
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        spannableStringForgetPassword.setSpan(clickableSpanForgetPassword, 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.textViewForgetPassword.setText(spannableStringForgetPassword);
        binding.textViewForgetPassword.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textViewForgetPassword.setHighlightColor(Color.TRANSPARENT);
    }

    /*
    Attempt to login using inputted email and password
     */
    private void login(String email, String password) {
        if (!validateForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        // Sign in failure
                        Log.w("Login", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Invalid login credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*
    Log in with Google authentication
     */
    private void loginWithGoogle() {
        GetSignInIntentRequest signInRequest = GetSignInIntentRequest.builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();
        signInClientWithGoogle.getSignInIntent(signInRequest)
                .addOnSuccessListener(this::launchGoogleLogin)
                .addOnFailureListener(e -> Log.e("Login", "Google Sign-in failed", e));
    }

    /*
    Check whether login input is valid
     */
    private boolean validateForm() {
        String email = binding.enterEmailEditText.getText().toString();
        String password = binding.enterPasswordEditText.getText().toString();
        // Check whether email field is empty
        if (TextUtils.isEmpty(email)) {
            binding.enterEmailEditText.setError("Required");
            Toast.makeText(getApplicationContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check whether password field is empty
        if (TextUtils.isEmpty(password)) {
            binding.enterPasswordEditText.setError("Required");
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Check whether email is valid
        if (!email.matches(EMAIL_REGEX)) {
            Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
    Activate launcher for Google login
     */
    private void launchGoogleLogin(PendingIntent pendingIntent) {
        try {
            signInLauncherWithGoogle.launch(new IntentSenderRequest.Builder(pendingIntent).build());
        } catch (Exception e) {
            Log.e("Login", "Couldn't start Google Sign In: " + e.getLocalizedMessage());
        }
    }

    /*
    Determine whether Google login was successful
     */
    private void handleLoginResultWithGoogle(Intent data) {
        try {
            // Login success
            firebaseAuthWithGoogle(signInClientWithGoogle.getSignInCredentialFromIntent(data).getGoogleIdToken());
        } catch (ApiException e) {
            // Login failure
            Log.w("Login", "Google sign in failed", e);
        }
    }

    /*
    Authenticate Google login with Firebase
     */
    private void firebaseAuthWithGoogle(String idToken) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        // Login failure
                        Log.w("Login", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}