package com.example.calendarmemories;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class CoupleFragment extends Fragment {

    private static final String COUPLE_KEY = "coupleUID";
    private static final String[] TEST_ARRAY = { "hello", "hell", "heaven", "holy", "molly" };

    private View v;
    private FragmentContainerView mainFragmentContainer;
    private LinearLayout noCoupleAlert;
    private TextView noAccountAlert;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String coupleUID;

    public CoupleFragment() {
        // Required empty public constructor
        mAuth = FirebaseAuth.getInstance();
    }

    public CoupleFragment(SharedPreferences sharedPref, FirebaseAuth mAuth) {
        this.sharedPref = sharedPref;
        this.mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_couple, container, false);

        mainFragmentContainer = v.findViewById(R.id.mainFragmentContainer);
        noCoupleAlert = v.findViewById(R.id.noCoupleAlert);
        noAccountAlert = v.findViewById(R.id.noAccountAlert);

        isCoupleUpdatePanel();

        return v;
    }
    public void isCoupleUpdatePanel() {
        if (user == null || user.isAnonymous()) {
            anonymousUpdatePanel();
        } else {
            System.out.println("Logged in");
            DocumentReference userDB = db.document(ViewHelper.getPathForAccountInfo(user.getUid()));
            userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            String couple = (String) data.get(COUPLE_KEY);
                            if (couple == null) {
                                noCoupleUpdatePanel();
                            } else {
                                coupleUpdatePanel(couple);
                            }
                        } else {
                            noCoupleUpdatePanel();
                        }
                    } else {
                        // #TODO: Failed task response
                        System.out.println(task.getException());
                    }
                }
            });
        }
    }

    public void coupleUpdatePanel(String couple) {
        noCoupleAlert.setVisibility(View.GONE);
        noAccountAlert.setVisibility(View.GONE);
        mainFragmentContainer.setVisibility(View.VISIBLE);
        coupleUID = couple;
    }

    public void noCoupleUpdatePanel() {
        noCoupleAlert.setVisibility(View.VISIBLE);
        noAccountAlert.setVisibility(View.GONE);
        mainFragmentContainer.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, TEST_ARRAY);
        ((MaterialAutoCompleteTextView) v.findViewById(R.id.couplePageUserSearch)).setAdapter(adapter);
        coupleUID = null;
    }

    public void anonymousUpdatePanel() {
        noAccountAlert.setVisibility(View.VISIBLE);
        noCoupleAlert.setVisibility(View.GONE);
        mainFragmentContainer.setVisibility(View.GONE);
        coupleUID = null;
    }
}