package com.example.calendarmemories;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class SocialFragment extends Fragment {

    private static final int HOME_TAB = 0;
    private static final int SEARCH_TAB = 1;

    private View v;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> usernames;

    public SocialFragment() {
        // Required empty public constructor
    }

    public SocialFragment(SharedPreferences sharedPref, FirebaseAuth mAuth) {
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
        v = inflater.inflate(R.layout.fragment_social, container, false);

        tabLayout = v.findViewById(R.id.mainTabNavigator);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case SEARCH_TAB:
                        // #TODO: Response for search tab
                        fragment = new SocialHomeFragment();
                        break;
                    case HOME_TAB:
                    default:
                        fragment = new SocialHomeFragment(sharedPref, mAuth);
                        break;
                }
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.mainFragmentContainer, fragment)
                        .setReorderingAllowed(true)
                        .commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return v;
    }

    public void loadUsernames() {
        DocumentReference userDB = db.document(DBHelper.getPathForUsernames());
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (!documentSnapshot.exists()) return;
                    Map<String, Object> data = documentSnapshot.getData();
                    for (Map.Entry<String, Object> entry: data.entrySet()) {
                        Map<String, Object> entryData = (Map<String, Object>) entry.getValue();
                        usernames.add(entryData.get("username").toString());
                    }
                    // #TODO: Check this when updating SocialFragment class
                } else {
                    // #TODO: Failed task response
                }
            }
        });
    }
}