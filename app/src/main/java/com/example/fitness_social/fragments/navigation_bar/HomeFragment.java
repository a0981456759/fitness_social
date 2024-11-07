package com.example.fitness_social.fragments.navigation_bar;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fitness_social.R;
import com.example.fitness_social.activities.HomeActivity;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

}