package com.example.calendarmemories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import android.net.Uri;

import java.net.URI;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private static float FOOD_NAME_TEXT_SIZE = 18f;
    private static float MEAL_TYPE_TEXT_SIZE = 10f;
    private static float FOOD_IMAGE_WEIGHT = 1f;
    private static float FOOD_TEXT_WEIGHT = 15f;
    private static float FOOD_BTN_WEIGHT = 0f;
    private static int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
    private static int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;
    private static String DATE_PICKER_TITLE = "select date";

    private Button dailyDateBtn, leftDateBtn, rightDateBtn;
    private Button listViewToggleBtn, galleryViewToggleBtn;
    private FloatingActionButton floatingBtn;
    private LocalDate date;
    private View v;
    private ConstraintLayout constraintLayout;
    private File currentFile = null;
    private static DailyMemory dailyMemory;
    private LinearLayout.LayoutParams layoutParams;
    private static LinearLayout foodLinearLayout;

    private static String userDataDir = "userData";
    private String userID = "tempUser";
    private static String foodDir = "foodData";

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

        // #TODO: Start panel
        date = Time.getTodaysDate();
        getDailyMemory();

        dailyDateBtn = v.findViewById(R.id.dailyDateBtn);
        dailyDateBtn.setText(Time.toString(date));
        dailyDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(DATE_PICKER_TITLE)
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
                // #TODO: Remove programmed string
                picker.show(getChildFragmentManager(), "Tag");
                picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                            calendar.setTimeInMillis((Long) selection);
                            LocalDate newDate = LocalDate.of(calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DAY_OF_MONTH) + 1);
                            setDayTo(newDate);
                        }
                    }
                );
            }
        });
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
        listViewToggleBtn = v.findViewById(R.id.listViewToggleBtn);
        listViewToggleBtn.setSelected(true);
        galleryViewToggleBtn = v.findViewById(R.id.galleryViewToggleBtn);
        galleryViewToggleBtn.setSelected(false);
        listViewToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewToggleBtn.setSelected(true);
                galleryViewToggleBtn.setSelected(false);
                // #TODO: View toggle
            }
        });
        galleryViewToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewToggleBtn.setSelected(false);
                galleryViewToggleBtn.setSelected(true);
            }
        });
        return v;
    }

    public void setDayTo(LocalDate day) {
        date = day;
        dailyDateBtn.setText(Time.toString(date));
        getDailyMemory();
    }

    public void goYesterday() {
        date = Time.getYesterday(date);
        dailyDateBtn.setText(Time.toString(date));
        getDailyMemory();
    }

    public void goTomorrow() {
        date = Time.getTomorrow(date);
        dailyDateBtn.setText(Time.toString(date));
        getDailyMemory();
    }

    @Deprecated
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
        // #TODO: updatePanel causes the app to crash
        // Remove all previous views in the panel
        for (int i = foodLinearLayout.getChildCount() - 1; i > 0; i--) {
            foodLinearLayout.removeViewAt(i);
        }
        // Add views in the panel
        System.out.println("Food size:" + dailyMemory.getNumFood());
        for (int i = 0; i < dailyMemory.getNumFood(); i++) {
            Food food = dailyMemory.getFoodAt(i);
            foodLinearLayout.addView(getCardListView(food));
            // #TODO: Finish up creating a card for foods
        }
    }

    public void updatePanelDebug() {
        System.out.println("MyDebug" + foodLinearLayout.getChildCount());
    }

    @Deprecated
    public LinearLayout getCardListViewDeprecated(Food food) {
        LinearLayout container = new LinearLayout(getContext());
        container.setLayoutParams(getLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0));
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParamMPWC = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams layoutParam0dpWC = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        MaterialCardView card = new MaterialCardView(getContext());
        card.setLayoutParams(getLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0));
        LinearLayout cardSubLL = new LinearLayout(getContext());
        cardSubLL.setLayoutParams(getLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0));
        cardSubLL.setOrientation(LinearLayout.HORIZONTAL);
        cardSubLL.setGravity(Gravity.CENTER_VERTICAL);

        // Creates an imageview holder
        layoutParam0dpWC.weight = FOOD_IMAGE_WEIGHT;
        ImageView foodImg = new ImageView(getContext());
        foodImg.setLayoutParams(getLayoutParams(0, WRAP_CONTENT, FOOD_IMAGE_WEIGHT));
        foodImg.setImageResource(R.drawable.ic_food_sign);

        // Creates a subsub layout and add textViews to it
        LinearLayout subsubLL = new LinearLayout(getContext());
        subsubLL.setLayoutParams(getLayoutParams(0, WRAP_CONTENT, FOOD_TEXT_WEIGHT));
        subsubLL.setOrientation(LinearLayout.VERTICAL);
        TextView foodNameText = getTextView(getContext(), layoutParamMPWC, food.getFoodName());
        foodNameText.setTypeface(Typeface.DEFAULT_BOLD);
        foodNameText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FOOD_NAME_TEXT_SIZE);
        TextView mealTypeText = getTextView(getContext(), layoutParamMPWC, food.getMealType());
        mealTypeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, MEAL_TYPE_TEXT_SIZE);
        TextView withWhoText = getTextView(getContext(), layoutParamMPWC, food.getWithWho());
        TextView sideNotesText = getTextView(getContext(), layoutParamMPWC, food.getSideNotes());

        // Create a deletebtn
        layoutParam0dpWC.weight = 1f;
        LinearLayout btnLL = new LinearLayout(getContext());
        btnLL.setLayoutParams(getLayoutParams(WRAP_CONTENT, WRAP_CONTENT, 0));
        btnLL.setOrientation(LinearLayout.VERTICAL);
        Button deleteBtn = (Button) getLayoutInflater().inflate(R.layout.custom_button_layout, null);
        deleteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete_icon, 0, 0, 0);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Delete btn clicked");
                foodLinearLayout.removeView(container);
                deleteFoodFromDatabase(food);
            }
        });
        Button editBtn = (Button) getLayoutInflater().inflate(R.layout.custom_button_layout, null);
        editBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_icon, 0, 0, 0);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: Edit btn click response
            }
        });

        MaterialDivider materialDivider = new MaterialDivider(getContext());
        materialDivider.setLayoutParams(getLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0));

        btnLL.addView(editBtn);
        btnLL.addView(deleteBtn);

        subsubLL.addView(foodNameText);
        subsubLL.addView(mealTypeText);
        subsubLL.addView(withWhoText);
        subsubLL.addView(sideNotesText);

        cardSubLL.addView(foodImg);
        cardSubLL.addView(subsubLL);
        cardSubLL.addView(btnLL);

        card.addView(cardSubLL);

        container.addView(card);
        container.addView(materialDivider);

        return container;
    }

    public LinearLayout getCardListView(Food food) {
        LinearLayout container = (LinearLayout) getLayoutInflater().inflate(R.layout.list_view_test, null);
        ImageView imgView = container.findViewById(R.id.listCardImageContainer);
        ((TextView) container.findViewById(R.id.listCardFoodNameTxt)).setText(food.getFoodName());
        ((TextView) container.findViewById(R.id.listCardMealTypeTxt)).setText(food.getMealType());
        ((TextView) container.findViewById(R.id.listCardWithWhoTxt)).setText(food.getWithWho());
        ((TextView) container.findViewById(R.id.listCardSideNotesTxt)).setText(food.getSideNotes());
        Button deleteBtn = container.findViewById(R.id.listCardDeleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodLinearLayout.removeView(container);
                deleteFoodFromDatabase(food);
            }
        });
        Button editBtn = container.findViewById(R.id.listCardEditBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: Edit btn action
            }
        });

        ViewTreeObserver viewTreeObserver = imgView.getViewTreeObserver();
        viewTreeObserver
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (food.getImageFilePath() != null) {
                            System.out.println("Debug trying to get file at " + food.getImageFilePath());
                            File file = new File(getContext().getFilesDir(), food.getImageFilePath());
                            System.out.println("Does file exists? " + file.exists());
                            Picasso.get().load(file)
                                    .resize(imgView.getWidth(), imgView.getWidth())
                                    .centerCrop()
                                    .into(imgView);
                        }
                        imgView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        return container;
    }

    private LinearLayout.LayoutParams getLayoutParams(int width, int height, float weight) {
        LinearLayout.LayoutParams result = new LinearLayout.LayoutParams(width, height);
        result.weight = weight;
        return result;
    }

    public TextView getTextView(Context context, ViewGroup.LayoutParams layoutParams, String text) {
        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        return textView;
    }

    public void addFood(Food food) {
        food.setFoodID(dailyMemory.generateFoodID());
        dailyMemory.addFood(food);

        // #TODO: Later integrate this to DailyMemory object
        String filePath = date.toString() + food.getFoodID();
        food.setImageFilePath(filePath);
        System.out.println("Debug ImgFilePath saved to: " + food.getImageFilePath());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), food.getImageUri());
        } catch (Exception e) {
            //#TODO: File not found exception
            System.out.println("Debug bitMap failed to load");
        }
        try (FileOutputStream fos = getContext().openFileOutput(filePath, Context.MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            // #TODO: File out exception
            System.out.println("Debug file cannot be written");
        }

        foodLinearLayout.addView(getCardListView(food));
        addFoodToDatabase(food);
    }

    public void deleteFoodFromDatabase(Food food) {
        DocumentReference userDB = db.document(getDatabasePath());
        Map<String, Object> dataToDelete = new HashMap<String, Object>();
        dataToDelete.put(food.getFoodID(), FieldValue.delete());
        userDB.update(dataToDelete)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // #TODO: Successful log
                        System.out.println("Successfully deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // #TODO: Failure log
                        System.out.println("Deletion failed");
                        System.out.println(food.getFoodID());
                    }
                });
    }

    public void addFoodToDatabase(Food food) {
        DocumentReference userDB = db.document(getDatabasePath());
        Map<String, Object> dataToAdd = new HashMap<String, Object>();
        dataToAdd.put(DailyMemory.NUM_FOOD_KEY, dailyMemory.getNumFood());
        dataToAdd.put(food.getFoodID(), food);
        userDB.set(dataToAdd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(foodLinearLayout, R.string.food_add_success_msg,
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(foodLinearLayout, R.string.food_add_failed_msg,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public String getDatabasePath() {
        return joinPath(userDataDir, userID, foodDir, Time.getDayID(date));
    }

    public String joinPath(String ... dirs) {
        String path = "";
        for (int i = 0; i < dirs.length; i++) {
            if (i != 0) path += "/";
            path += dirs[i];
        }
        return path;
    }

    public void getDailyMemory() {
        DocumentReference userDB = db.document(getDatabasePath());
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    dailyMemory = new DailyMemory(document);
                    updatePanel();
                } else {
                    // #TODO: Failed task response
                    System.out.println("Task not successful");
                }
            }
        });
    }
}