package com.example.fitness_social.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.fitness_social.R;
import com.example.fitness_social.activities.HomeActivity;
import com.example.fitness_social.activities.SettingsActivity;
import com.example.fitness_social.fragments.navigation_bar.SettingsFragment;
import com.example.fitness_social.tables.UserInfo;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.PhoneNumberUtil;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class MyProfileFragment extends Fragment {

    private ImageView userEditImage;
    private ActivityResultLauncher<String[]> intentLauncher;
    private Uri mFileUri = null;
    private FirebaseUser currentUser;
    private UserInfo userInfo;
    private UserInfo currentUserInfo = null;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), fileUri -> {
                    if (fileUri != null) {
                        mFileUri = fileUri;
                        Glide.with(getActivity())
                                .load(mFileUri)
                                .into(userEditImage);
                    } else {
                        Log.w("MyProfileFragment", "File URI is null");
                    }
                });
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        initPage(rootView);
        setAllOnClick(rootView);
        setGoBack();

        return rootView;
    }

    private void setGoBack() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initPage(View rootView) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            String value = arguments.getString("isNewUser");
            EditText enterUsernameEditText = rootView.findViewById(R.id.enterUsernameEditText);

            Spinner spinnerPhoneNumber = rootView.findViewById(R.id.spinnerPhoneNumber);
            ArrayList<String> regionList = getCountryCodes();
            ArrayAdapter<String> phoneNumberAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, regionList);
            spinnerPhoneNumber.setAdapter(phoneNumberAdapter);

            EditText enterPhoneNumberText = rootView.findViewById(R.id.enterPhoneNumberText);

            EditText ageEditText = rootView.findViewById(R.id.ageEditText);

            EditText weightEditText = rootView.findViewById(R.id.weightEditText);

            EditText heightEditText = rootView.findViewById(R.id.heightEditText);

            spinnerPhoneNumber.setSelection(0);

            if (value.equals("0")) {
                UserInfo.selectAll(new UserInfo.DataCallback() {
                    @Override
                    public void onCallback(ArrayList<UserInfo> allUserInfo) {
                        String currentUid = currentUser.getUid();
                        for (int i = 0; i < allUserInfo.size(); i++) {
                            if (Objects.equals(allUserInfo.get(i).uid, currentUid)) {
                                currentUserInfo = allUserInfo.get(i);
                                // Log.d("Find current user",currentUserInfo.uid);
                                break;
                            }
                        }
                        if(currentUserInfo == null) {
                            Log.e("MyFragment", "Unknown error, currentUserInfo == null");
                            return;
                        }
                        if(!currentUserInfo.profile_url.isEmpty()) {
                            Glide.with(getActivity())
                                    .load(currentUserInfo.profile_url)
                                    .into(userEditImage);
                        }
                        enterUsernameEditText.setText(currentUserInfo.user_name);
                        if(!currentUserInfo.phone_number.isEmpty()) {
                            String phoneNumberPart1 = currentUserInfo.phone_number.substring(0,currentUserInfo.phone_number.indexOf(")") + 1);
                            String phoneNumberPart2 = currentUserInfo.phone_number.substring(currentUserInfo.phone_number.indexOf(")") + 2);
                            enterPhoneNumberText.setText(phoneNumberPart2);

                            Spinner spinnerPhoneNumber = rootView.findViewById(R.id.spinnerPhoneNumber);
                            ArrayList<String> regionList = getCountryCodes();
                            for (int i = 1; i<regionList.size();i++) {
                                String region1 = regionList.get(i);
                                if(region1.equals(phoneNumberPart1)) {
                                    spinnerPhoneNumber.setSelection(i);
                                }
                            }
                        }
                        // Init age spinner
                        Log.d("initAge",""+currentUserInfo.age);
                        if(currentUserInfo.age == -1) {
                            ;
                        }
                        else {
                            ageEditText.setText(currentUserInfo.age+"");
                        }

                        // Init weight spinner
                        if(currentUserInfo.weight == -1) {
                            ;
                        }
                        else {
                            weightEditText.setText(currentUserInfo.weight+"");
                        }

                        // Init height spinner
                        if(currentUserInfo.weight == -1) {
                            ;
                        }
                        else {
                            heightEditText.setText(currentUserInfo.height+"");
                        }
                    }
                });
            }
        }
        else {
            Log.e("MyFragment", "No argument! ");
        }
    }

    private void setAllOnClick(View rootView) {
        setProfileOnClick(rootView);
        setSaveButtonOnClick(rootView);

        ImageButton backButton = rootView.findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setProfileOnClick(View rootView) {
        userEditImage = rootView.findViewById(R.id.userEditImage);
        userEditImage.setOnClickListener(view -> {
            intentLauncher.launch(new String[]{"image/*"});
        });
    }

    private void setSaveButtonOnClick(View rootView) {
        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText enterUsernameEditText = rootView.findViewById(R.id.enterUsernameEditText);
                Spinner spinnerPhoneNumber = rootView.findViewById(R.id.spinnerPhoneNumber);
                EditText enterPhoneNumberText = rootView.findViewById(R.id.enterPhoneNumberText);
                EditText ageEditText = rootView.findViewById(R.id.ageEditText);
                EditText weightEditText = rootView.findViewById(R.id.weightEditText);
                EditText heightEditText = rootView.findViewById(R.id.heightEditText);

                if (!validateForm(rootView)) {
                    return;
                }
                if (currentUserInfo == null) {
                    currentUserInfo = new UserInfo(currentUser.getUid(), enterUsernameEditText.getText().toString(),
                            "", currentUser.getEmail());
                }
                currentUserInfo.user_name = enterUsernameEditText.getText().toString();
                if (!enterPhoneNumberText.getText().toString().isEmpty()) {
                    currentUserInfo.phone_number = spinnerPhoneNumber.getSelectedItem().toString()
                            + " " + enterPhoneNumberText.getText().toString();
                }
                if (!ageEditText.getText().toString().isEmpty()) {
                    currentUserInfo.age = Integer.parseInt(ageEditText.getText().toString());
                }
                if (!weightEditText.getText().toString().isEmpty()) {
                    currentUserInfo.weight = Integer.parseInt(weightEditText.getText().toString());
                }
                if (!heightEditText.getText().toString().isEmpty()) {
                    currentUserInfo.height = Integer.parseInt(heightEditText.getText().toString());
                }
                if(mFileUri != null) {
                    uploadFromUri(mFileUri);
                }
                else {
                    UserInfo.insertAndUpdate(currentUserInfo);
                    Bundle arguments = getArguments();
                    String value = arguments.getString("isNewUser");
                    if (value.equals("1")) {
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        startActivity(intent);

                    }
                }
            }
        });
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d("setProfileOnClick", "userProfile" + currentUser.getUid());

        // Save the File URI
        mFileUri = fileUri;

        // Get a reference to the Firebase Storage where we will upload the file
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference fileReference = storageRef.child("userProfile/" + currentUser.getUid());

        // Upload the selected file to Firebase Storage
        fileReference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();

                        // Get download URL and log it
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.d("setProfileOnClick", "Download URL: " + uri.toString());
                            currentUserInfo.profile_url = uri.toString();
                            UserInfo.insertAndUpdate(currentUserInfo);

                            // Direct to new page
                            Bundle arguments = getArguments();
                            String value = arguments.getString("isNewUser");


                            if (value.equals("1")) {
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                                startActivity(intent);

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.e("setProfileOnClick", "Upload failed", exception);
                        Toast.makeText(getContext(), "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateForm(View rootView) {
        boolean valid = true;
        EditText usernameEditText = rootView.findViewById(R.id.enterUsernameEditText);
        Spinner spinnerPhoneNumber = rootView.findViewById(R.id.spinnerPhoneNumber);
        EditText enterPhoneNumberText = rootView.findViewById(R.id.enterPhoneNumberText);
        String username = usernameEditText.getText().toString();
        String reginNumber = spinnerPhoneNumber.getSelectedItem().toString();
        String phoneNumber = enterPhoneNumberText.getText().toString();
        EditText ageEditText = rootView.findViewById(R.id.ageEditText);
        EditText weightEditText = rootView.findViewById(R.id.weightEditText);
        EditText heightEditText = rootView.findViewById(R.id.heightEditText);
        String age = ageEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        String height = heightEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            valid = false;
            usernameEditText.setError("Required.");
            Toast.makeText(getContext(), "Please enter username!", Toast.LENGTH_SHORT).show();
            return valid;
        } else {
            if (username.length() < 4 || username.length() >= 20) {
                valid = false;
                Toast.makeText(getContext(), "Username must be between 4 and 20 characters long!", Toast.LENGTH_SHORT).show();
                usernameEditText.setError("Invalid.");
                return valid;
            }
            else {
                if(!username.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    valid = false;
                    Toast.makeText(getContext(), "Username must start with a letter and consist of letters and numbers", Toast.LENGTH_SHORT).show();
                    usernameEditText.setError("Invalid.");
                    return valid;
                }
            }
        }

        if((reginNumber.equals("Select")&&!phoneNumber.isEmpty())||(!reginNumber.equals("Select")&&phoneNumber.isEmpty())) {
            valid = false;
            Toast.makeText(getContext(), "Please enter valid phone number!", Toast.LENGTH_SHORT).show();
            enterPhoneNumberText.setError("Invalid.");
            return valid;
        }
        else if(!phoneNumber.matches("^[0-9]+$")&&!phoneNumber.isEmpty()) {
            valid = false;
            Toast.makeText(getContext(), "Please enter valid phone number!", Toast.LENGTH_SHORT).show();
            enterPhoneNumberText.setError("Invalid.");
            return valid;
        }

        if(!age.matches("^[1-9][0-9]*$")&&!age.isEmpty()) {
            valid = false;
            Toast.makeText(getContext(), "Please enter an integer age!", Toast.LENGTH_SHORT).show();
            ageEditText.setError("Invalid.");
            return valid;
        }else if (!age.isEmpty() && (Integer.parseInt(age)<0 || Integer.parseInt(age)>100) ) {
            valid = false;
            Toast.makeText(getContext(), "Please enter your age from 0 to 100!", Toast.LENGTH_SHORT).show();
            ageEditText.setError("Invalid.");
            return valid;
        }

        if(!weight.matches("^[1-9][0-9]*$")&&!weight.isEmpty()) {
            valid = false;
            Toast.makeText(getContext(), "Please enter an integer weight!", Toast.LENGTH_SHORT).show();
            weightEditText.setError("Invalid.");
            return valid;
        }else if (!weight.isEmpty() && ( Integer.parseInt(weight)<30 || Integer.parseInt(weight)>500)) {
            valid = false;
            Toast.makeText(getContext(), "Please enter your weight from 30 to 500!", Toast.LENGTH_SHORT).show();
            weightEditText.setError("Invalid.");
            return valid;
        }

        if(!height.matches("^[1-9][0-9]*$")&&!height.isEmpty()) {
            valid = false;
            Toast.makeText(getContext(), "Please enter an integer height!", Toast.LENGTH_SHORT).show();
            heightEditText.setError("Invalid.");
            return valid;
        }else if (!height.isEmpty() && (Integer.parseInt(height)<50 || Integer.parseInt(height)>250)) {
            valid = false;
            Toast.makeText(getContext(), "Please enter your height from 50 to 250!", Toast.LENGTH_SHORT).show();
            heightEditText.setError("Invalid.");
            return valid;
        }
        usernameEditText.setError(null);
        enterPhoneNumberText.setError(null);
        ageEditText.setError(null);
        weightEditText.setError(null);
        heightEditText.setError(null);
        return valid;
    }

    private ArrayList<String> getCountryCodes() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Set<String> regions = phoneUtil.getSupportedRegions(); // Get all supported regions

        ArrayList<String> countryCodes = new ArrayList<>();
        for (String region : regions) {
            int countryCode = phoneUtil.getCountryCodeForRegion(region); // Get country code for the region
            countryCodes.add("+" + countryCode + " (" + region + ")");
        }

        Collections.sort(countryCodes, (code1, code2) -> {
            String region1 = code1.split(" ")[1];
            String region2 = code2.split(" ")[1];
            return region1.compareTo(region2);
        });

        countryCodes.add(0,"Select");
        return countryCodes;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LinearLayout logoutLinearLayout = getActivity().findViewById(R.id.logout);
        logoutLinearLayout.setEnabled(true);
        LinearLayout myProfileLinearLayout = getActivity().findViewById(R.id.myProfileSetting);
        myProfileLinearLayout.setEnabled(true);
    }

}
