package com.example.calendarmemories;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import android.net.Uri;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private static final float FOOD_NAME_TEXT_SIZE = 18f;
    private static final float MEAL_TYPE_TEXT_SIZE = 10f;
    private static final float FOOD_IMAGE_WEIGHT = 1f;
    private static final float FOOD_TEXT_WEIGHT = 15f;
    private static final int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;
    private static final int TAKE_PHOTO = -1;
    private static final int SELECT_PHOTO = -2;
    private static final String IMAGE_MIME_TYPE = "image/png";
    private static final String DATE_PICKER_TITLE = "select date";
    public static final String WITH_WHO_TEXT_PREFIX = "Ate with: ";

    private Button dailyDateBtn, leftDateBtn, rightDateBtn;
    private Button listViewToggleBtn, galleryViewToggleBtn;
    private FloatingActionButton floatingBtn;
    private LocalDate date;
    private View v;
    private ConstraintLayout constraintLayout;
    private File currentFile = null;
    private DailyMemory dailyMemory;
    private LinearLayout foodLinearLayout;

    // #TODO: User authentification
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
                        .setSelection(Time.inMillis(date))
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
                AddFragment myAddFragment = new AddFragment(dailyMemory.generateFoodID());
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
        // #TODO: updatePanel causes the app to crash
        // Remove all previous views in the panel
        for (int i = foodLinearLayout.getChildCount() - 1; i > 0; i--) {
            foodLinearLayout.removeViewAt(i);
        }
        // Add views in the panel
        for (int i = 0; i < dailyMemory.getNumFood(); i++) {
            Food food = dailyMemory.getFoodAt(i);
            foodLinearLayout.addView(getCardListView(food));
            // #TODO: Finish up creating a card for foods
        }
    }

    @Deprecated
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
        setTextWithPrefix(container.findViewById(R.id.listCardWithWhoTxt), WITH_WHO_TEXT_PREFIX,
                food.getWithWho());
        ((TextView) container.findViewById(R.id.listCardSideNotesTxt)).setText(food.getSideNotes());
        Button deleteBtn = container.findViewById(R.id.listCardDeleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: Change the title of the builder
                getAlertDialogBuilder(R.string.delete_alert_title, R.string.delete_alert_msg)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                foodLinearLayout.removeView(container);
                                deleteFoodFromDatabase(food);
                                getSnackbar(foodLinearLayout, R.string.food_deleted_msg,
                                        Snackbar.LENGTH_SHORT, floatingBtn).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .show();
            }
        });
        Button editBtn = container.findViewById(R.id.listCardEditBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFragment myAddFragment = new AddFragment(food, container, dailyMemory.generateFoodID());
                myAddFragment.show(
                        getChildFragmentManager(), AddFragment.TAG
                );
            }
        });

        ViewTreeObserver viewTreeObserver = imgView.getViewTreeObserver();
        viewTreeObserver
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (food.getImageFilePath() != null) {
                            Uri imageUri = Uri.parse(food.getImageFilePath());
                            Glide.with(getContext()).load(imageUri)
                                    .centerCrop()
                                    .apply(new RequestOptions().override(imgView.getWidth()))
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
        dailyMemory.addFood(food);

        foodLinearLayout.addView(getCardListView(food));
        addFoodToDatabase(food);
    }

    public static void setTextWithPrefix(TextView textView, String prefix, String suffix) {
        textView.setText(prefix + suffix);
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
        dataToAdd.put(DailyMemory.ID_TEMPLATE_KEY, dailyMemory.getIDTemplate());
        dataToAdd.put(food.getFoodID(), food);
        userDB.set(dataToAdd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        getSnackbar(foodLinearLayout, R.string.add_success_msg,
                                Snackbar.LENGTH_SHORT, floatingBtn).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getSnackbar(foodLinearLayout, R.string.add_failed_msg,
                                Snackbar.LENGTH_LONG, floatingBtn).show();
                    }
                });
    }

    public void updateFoodToDatabase(Food food) {
        DocumentReference userDB = db.document(getDatabasePath());
        Map<String, Object> dataToModify = new HashMap<String, Object>();
        dataToModify.put(food.getFoodID(), food);
        userDB.update(dataToModify)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        getSnackbar(foodLinearLayout, R.string.modify_success_msg,
                                Snackbar.LENGTH_SHORT, floatingBtn).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getSnackbar(foodLinearLayout, R.string.modify_failed_msg,
                                Snackbar.LENGTH_LONG, floatingBtn).show();
                    }
                });
    }

    private String getDatabasePath() {
        return joinPath(userDataDir, userID, foodDir, Time.getDayID(date));
    }

    private String joinPath(String ... dirs) {
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
                    dailyMemory = new DailyMemory(document, date);
                    updatePanel();
                } else {
                    // #TODO: Failed task response
                    System.out.println("Task not successful");
                }
            }
        });
    }

    private MaterialAlertDialogBuilder getAlertDialogBuilder(int titleRes, int msgRes) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(titleRes)
                .setMessage(msgRes);
        return builder;
    }

    private Snackbar getSnackbar(View v, int msgRes, int duration, View anchor) {
        if (anchor == null) {
            return Snackbar.make(v, msgRes, duration);
        } else {
            return Snackbar.make(v, msgRes, duration).setAnchorView(anchor);
        }
    }
}