package com.example.calendarmemories;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SocialHomeFragment extends Fragment {

    private View v;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public SocialHomeFragment() {
        // Required empty public constructor
        this.mAuth = FirebaseAuth.getInstance();
    }

    public SocialHomeFragment(SharedPreferences sharedPref, FirebaseAuth mAuth) {
        this.sharedPref = sharedPref;
        this.mAuth = mAuth;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_social_home, container, false);

        user = mAuth.getCurrentUser();


        return v;
    }
}