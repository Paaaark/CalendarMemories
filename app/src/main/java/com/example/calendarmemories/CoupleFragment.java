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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoupleFragment extends Fragment {

    private static final String COUPLE_KEY = "coupleUID";
    private static final String COUPLE_CODE_KEY = "coupleCode";
    private static final String[] TEST_ARRAY = { "hello", "hell", "heaven", "holy", "molly" };

    private View v;
    private FragmentContainerView mainFragmentContainer;
    private LinearLayout noCoupleAlert, searchedPartner;
    private TextView noAccountAlert;
    private TextInputLayout coupleSearchLayout;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String coupleCode;
    private HashMap<String, String> usernameToUID;
    private ArrayList<String> usernames;

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
        coupleSearchLayout = v.findViewById(R.id.coupleSearchLayout);
        searchedPartner = v.findViewById(R.id.searchedPartner);
        usernames = new ArrayList<String>();
        usernameToUID = new HashMap<String, String>();

        isCoupleUpdatePanel();

        coupleSearchLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateCoupleCode();
            }
        });

        return v;
    }
    public void isCoupleUpdatePanel() {
        if (user == null || user.isAnonymous()) {
            anonymousUpdatePanel();
        } else {
            System.out.println("Logged in");
            DocumentReference userDB = db.document(DBHelper.getPathForAccountInfo(user.getUid()));
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
    }

    public void noCoupleUpdatePanel() {
        noCoupleAlert.setVisibility(View.VISIBLE);
        noAccountAlert.setVisibility(View.GONE);
        mainFragmentContainer.setVisibility(View.GONE);
        getCoupleCode();
    }

    public void anonymousUpdatePanel() {
        noAccountAlert.setVisibility(View.VISIBLE);
        noCoupleAlert.setVisibility(View.GONE);
        mainFragmentContainer.setVisibility(View.GONE);
    }

    public void updateCoupleCode(String code) {
        coupleCode = code;
        ((TextView) v.findViewById(R.id.coupleCode)).setText("Your couple code: " + coupleCode);
    }

    public void getCoupleCode() {
        DocumentReference userDB = db.document(DBHelper.getPathForAccountInfo(user.getUid()));
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (!documentSnapshot.exists()) return; // #TODO: Response to no document
                    Map<String, Object> data = documentSnapshot.getData();
                    String code = (String) data.get(COUPLE_CODE_KEY);
                    if (code == null) generateCoupleCode();
                    else {
                        updateCoupleCode(code);
                    }
                }
            }
        });
    }

    public void generateCoupleCode() {
        DocumentReference userDB = db.document(DBHelper.getPathForAccountInfo(user.getUid()));
        Map<String, Object> newCode = new HashMap<String, Object>();
        coupleCode = DBHelper.generateCoupleCode();
        newCode.put(COUPLE_CODE_KEY, coupleCode);
        userDB.update(newCode).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateCoupleCode(coupleCode);
            }
        });
    }

    public void validateCoupleCode() {
        TextInputEditText coupleSearchText = v.findViewById(R.id.coupleSearchText);
        String inputCode = coupleSearchText.getText().toString();
        System.out.println("Received inputCode is: " + inputCode);
        if (inputCode.length() != DBHelper.COUPLE_CODE_LEN) {
            ViewHelper.getSnackbar(v, R.string.invalid_couple_code, Snackbar.LENGTH_SHORT, null);
            return;
        }
        queryCoupleCode(inputCode);
    }

    public void queryCoupleCode(String code) {
        Query query = db.collection(DBHelper.USERS_DIR).whereEqualTo("coupleCode", code);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.size() == 0) {
                    ViewHelper.getSnackbar(v, R.string.invalid_couple_code, Snackbar.LENGTH_SHORT,
                            null).show();
                    return;
                }
                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                String username = (String) documentSnapshot.get("username");
                String name = (String) documentSnapshot.get("name");
                searchedPartner.setVisibility(View.VISIBLE);
                ((TextView) v.findViewById(R.id.searchedPartnerName)).setText(name);
                ((TextView) v.findViewById(R.id.searchedPartnerUsername)).setText(username);
            }
        });
    }
}