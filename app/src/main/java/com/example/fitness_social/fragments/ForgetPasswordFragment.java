package com.example.fitness_social.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.fitness_social.R;
import com.example.fitness_social.activities.LoginActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordFragment extends Fragment{

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forget_password, container, false);

        initPage();
        setAllOnClick(rootView);
        setGoBack();

        return rootView;
    }

    private void initPage() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setGoBack() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent login_intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(login_intent);
            }
        });
    }

    private void setAllOnClick(View rootView) {
        setContinueButtonOnClick(rootView);
    }

    private void setContinueButtonOnClick(View rootView) {
        // Handle continue button onClick (Reset password)
        Button continueButton = rootView.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueButton.setEnabled(false);
                TextView emailTextView = rootView.findViewById(R.id.enterEmailEditText);
                String email = emailTextView.getText().toString();
                if (!validateForm(rootView)) {
                    continueButton.setVisibility(View.VISIBLE);
                    continueButton.setEnabled(true);
                    return;
                }
                // Send password reset email
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                                continueButton.setVisibility(View.GONE);

                                TextView resetPasswordTextTextView = rootView.findViewById(R.id.resetPasswordText);
                                resetPasswordTextTextView.setVisibility(View.VISIBLE);

                                Button goBackButton = rootView.findViewById(R.id.goBackButton);
                                goBackButton.setEnabled(true);
                                goBackButton.setVisibility(View.VISIBLE);

                                goBackButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent login_intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(login_intent);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Send email fail!", Toast.LENGTH_SHORT).show();
                                continueButton.setVisibility(View.VISIBLE);
                                continueButton.setEnabled(true);
                            }
                        });

            }
        });
    }

    private boolean validateForm(View rootView) {
        boolean valid = true;
        EditText emailEditText = rootView.findViewById(R.id.enterEmailEditText);
        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Required.");
            valid = false;
        }
        if (!valid) {
            Toast.makeText(getContext(), "Please enter email!",
                    Toast.LENGTH_SHORT).show();
            return valid;
        }
        else {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                Toast.makeText(getContext(), "Invalid email address, please re-enter!",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return valid;
    }
}
