package com.example.calendarmemories;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private Button dailyDateBtn, leftDateBtn, rightDateBtn;
    private FloatingActionButton floatingBtn;
    private LocalDate date;
    private View v;
    private ConstraintLayout constraintLayout;
    private File currentFile = null;
    private static DailyMemory dailyMemory;
    private LinearLayout.LayoutParams layoutParams;
    private static LinearLayout foodLinearLayout;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_daily, null, false);
        v.setClickable(true);
        v.setFocusable(true);

        constraintLayout = v.findViewById(R.id.constraintLayout);
        foodLinearLayout = v.findViewById(R.id.foodLinearLayout);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // #TODO: For debug only
        dailyMemory = new DailyMemory();

        date = Time.getTodaysDate();
        updateDailyMemory();
        updatePanelDebug();
        //updatePanel();

        dailyDateBtn = v.findViewById(R.id.dailyDateBtn);
        dailyDateBtn.setText(Time.toString(date));
        leftDateBtn = v.findViewById(R.id.leftDateBtn);
        leftDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goYesterday();
            }
        });
        rightDateBtn = v.findViewById(R.id.rightDateBtn);
        rightDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTomorrow();
            }
        });
        floatingBtn = v.findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFragment myAddFragment = new AddFragment();
                myAddFragment.show(
                        getChildFragmentManager(), AddFragment.TAG
                );
                // #TODO: Floating btn click response
            }
        });
        return v;
    }

    public void goYesterday() {
        date = Time.getYesterday(date);
        dailyDateBtn.setText(Time.toString(date));
    }

    public void goTomorrow() {
        date = Time.getTomorrow(date);
        dailyDateBtn.setText(Time.toString(date));
    }

    public void updateDailyMemory() {
        // If we already have a file open, write in the file and close the file
        if (currentFile != null) {
            try (ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(currentFile))) {
                dailyMemory.updateFile(objOut);
            } catch (Exception e) {
                // #TODO: Exception handling
            }
        }
        // Else open a new file. If exists, read the data from it
        currentFile =  new File(getActivity().getFilesDir(), Time.toString(date));
        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
                dailyMemory = new DailyMemory();
                return;
            } catch (Exception e) {
                // #TODO: Exception handling
            }
        }
        try (ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(currentFile))) {
            dailyMemory = new DailyMemory(objIn);
        } catch (Exception e) {
            // #TODO: Exception handling
        }
    }

    public void updatePanel() {
        // #TODO: updatePanel
        // #TODO: updatePanel causes the game to crash
        // Remove all previous views in the panel
        for (int i = foodLinearLayout.getChildCount() - 1; i > 0; i--) {
            foodLinearLayout.removeViewAt(i);
        }
        // Add views in the panel
        for (int i = 0; i < dailyMemory.getNumFood(); i++) {
            Food food = dailyMemory.getFoodAt(i);
            // #TODO: Finish up creating a card for foods
            //MaterialCardView foodCard = new MaterialCardView();
        }
    }

    public void updatePanelDebug() {
        System.out.println("MyDebug" + foodLinearLayout.getChildCount());
    }

    public MaterialCardView getMaterialCardView(Food food) {
        // #TODO: Card Layout params method
        LinearLayout.LayoutParams layoutParamMPWC = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams layoutParam0dpWC = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        MaterialCardView card = new MaterialCardView(getContext());
        card.setLayoutParams(layoutParamMPWC);
        LinearLayout cardSubLL = new LinearLayout(getContext());
        cardSubLL.setLayoutParams(layoutParamMPWC);
        cardSubLL.setOrientation(LinearLayout.HORIZONTAL);
        cardSubLL.setGravity(Gravity.CENTER_VERTICAL);

        // Creates an imageview holder
        layoutParam0dpWC.weight = 1f;
        ImageView foodImg = new ImageView(getContext());
        foodImg.setLayoutParams(layoutParam0dpWC);
        foodImg.setImageResource(R.drawable.ic_food_sign);

        // Creates a subsub layout and add textViews to it
        layoutParam0dpWC.weight = 15f;
        LinearLayout subsubLL = new LinearLayout(getContext());
        subsubLL.setLayoutParams(layoutParam0dpWC);
        subsubLL.setOrientation(LinearLayout.VERTICAL);
        TextView foodNameText = getTextView(getContext(), layoutParamMPWC, food.getFoodName());
        TextView mealTypeText = getTextView(getContext(), layoutParamMPWC, food.getMealType());
        TextView withWhoText = getTextView(getContext(), layoutParamMPWC, food.getWithWho());
        TextView sideNotesText = getTextView(getContext(), layoutParamMPWC, food.getSideNotes());

        // Create a deletebtn
        layoutParam0dpWC.weight = 1f;
        ImageButton deleteBtn = new ImageButton(getContext());
        deleteBtn.setLayoutParams(layoutParam0dpWC);
        deleteBtn.setImageResource(R.drawable.ic_delete_icon);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Delete btn clicked");
                foodLinearLayout.removeView(card);
            }
        });

        subsubLL.addView(foodNameText);
        subsubLL.addView(mealTypeText);
        subsubLL.addView(withWhoText);
        subsubLL.addView(sideNotesText);

        cardSubLL.addView(foodImg);
        cardSubLL.addView(subsubLL);
        cardSubLL.addView(deleteBtn);

        card.addView(cardSubLL);

        return card;
    }

    public TextView getTextView(Context context, ViewGroup.LayoutParams layoutParams, String text) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        return textView;
    }

    public void addFood(Food food) {
        dailyMemory.addFood(food);
        foodLinearLayout.addView(getMaterialCardView(food));
        addFoodToDatabase(food);
    }

    public void addFoodToDatabase(Food food) {
        int numFood = dailyMemory.getNumFood();
        db.collection("sampleData").document("user1")
                .update("numFood", numFood)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Successfully logged");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error logging");
                    }
                });
        Map<String, Object> dataToAdd = new HashMap<String, Object>();
        dataToAdd.put("Food" + numFood, food);
        db.collection("sampleData").document("user1")
                .set(dataToAdd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Successfully logged");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error logging");
                    }
                });
    }
}