package com.example.calendarmemories;

import android.app.Dialog;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
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
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialFade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends DialogFragment {

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
    public static final String TAG = "DailyFragment";

    private Button dailyDateBtn, leftDateBtn, rightDateBtn;
    private Button listViewToggleBtn, galleryViewToggleBtn;
    private FloatingActionButton floatingBtn;
    private LocalDate date;
    private View v;
    private ConstraintLayout constraintLayout;
    private File currentFile = null;
    private DailyMemory dailyMemory;
    private LinearLayout foodLinearLayout;
    private GridLayout foodCardGridLayout;
    private int currentLayout = R.layout.list_view;

    private static String userDataDir = "userData";
    private static String foodDir = "foodData";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public DailyFragment() {
        super(R.layout.fragment_daily);
    }

    public DailyFragment(LocalDate date) {
        super(R.layout.fragment_daily);
        this.date = date;
    }

    public DailyFragment(int currentLayout, FirebaseAuth mAuth) {
        super(R.layout.fragment_daily);
        this.currentLayout = currentLayout;
        this.mAuth = mAuth;
        this.user = mAuth.getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels*0.85);
            int height = (int) (getResources().getDisplayMetrics().heightPixels*0.65);
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("onCreateView called");

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_daily, null, false);
        v.setClickable(true);
        v.setFocusable(true);

        constraintLayout = v.findViewById(R.id.constraintLayout);
        foodLinearLayout = v.findViewById(R.id.foodLinearLayout);
        foodCardGridLayout = v.findViewById(R.id.foodCardGridLayout);

        // #TODO: Start panel
        if (date == null) {
            date = Time.getTodaysDate();
        }
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
//                MaterialFade fade = new MaterialFade();
//                myAddFragment.setEnterTransition(fade);
//                TransitionManager.beginDelayedTransition(constraintLayout, fade);
                myAddFragment.show(
                        getChildFragmentManager(), AddFragment.TAG
                );
                // #TODO: Floating btn click response
            }
        });
        listViewToggleBtn = v.findViewById(R.id.listViewToggleBtn);
        listViewToggleBtn.setSelected(false);
        galleryViewToggleBtn = v.findViewById(R.id.galleryViewToggleBtn);
        galleryViewToggleBtn.setSelected(false);
        if (currentLayout == R.layout.gallery_view) {
            galleryViewToggleBtn.setSelected(true);
        } else {
            listViewToggleBtn.setSelected(true);
        }
        listViewToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewToggleBtn.setSelected(true);
                galleryViewToggleBtn.setSelected(false);
                currentLayout = R.layout.list_view;
                updatePanel();
            }
        });
        galleryViewToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewToggleBtn.setSelected(false);
                galleryViewToggleBtn.setSelected(true);
                currentLayout = R.layout.gallery_view;
                updatePanel();
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
        // Remove all previous views in the panel
        for (int i = foodCardGridLayout.getChildCount() - 1; i >= 0; i--) {
            foodCardGridLayout.removeViewAt(i);
        }
        if (currentLayout == R.layout.list_view) {
            foodCardGridLayout.setColumnCount(1);
        } else {
            foodCardGridLayout.setColumnCount(2);
        }
        // Add views in the panel
        for (int i = 0; i < dailyMemory.getNumFood(); i++) {
            Food food = dailyMemory.getFoodAt(i);
            foodCardGridLayout.addView(getCard(food));
            // #TODO: Finish up creating a card for foods
        }
    }

    public LinearLayout getCard(Food food) {
        System.out.println("Current layout:" + currentLayout);
        System.out.println("list_view:" + R.layout.list_view);
        LinearLayout container = (LinearLayout) getLayoutInflater().inflate(currentLayout, null);
        System.out.println(container.toString());

        ImageView imgView = container.findViewById(R.id.imageContainer);

        ((TextView) container.findViewById(R.id.foodName)).setText(food.getFoodName());
        ((TextView) container.findViewById(R.id.mealType)).setText(food.getMealType());
        setTextWithPrefix(container.findViewById(R.id.withWho), WITH_WHO_TEXT_PREFIX,
                food.getWithWho());
        ((TextView) container.findViewById(R.id.sideNotes)).setText(food.getSideNotes());
        Button deleteBtn = container.findViewById(R.id.cardDeleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: Change the title of the builder
                getAlertDialogBuilder(R.string.delete_alert_title, R.string.delete_alert_msg)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                foodCardGridLayout.removeView(container);
                                deleteFoodFromDatabase(food);
                                ViewHelper.getSnackbar(foodCardGridLayout, R.string.food_deleted_msg,
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
        Button editBtn = container.findViewById(R.id.cardEditBtn);
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
                        if (currentLayout == R.layout.gallery_view) {
                            imgView.setLayoutParams(new LinearLayout.LayoutParams(imgView.getWidth(),
                                    imgView.getWidth()));
                        }
                        if (food.getImageFilePath() != null) {
                            Uri imageUri = Uri.parse(food.getImageFilePath());
                            File file = new File(imageUri.getPath());
                            Glide.with(getContext()).load(imageUri)
                                    .centerCrop()
                                    .apply(new RequestOptions().override(imgView.getWidth()))
                                    .into(imgView);
                        }
                        imgView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: Update the panel according to the updates we got
                PicturesFragment myPicturesFragment = new PicturesFragment(food, imgView);
                myPicturesFragment.show(
                        getChildFragmentManager(), PicturesFragment.TAG
                );
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

        foodCardGridLayout.addView(getCard(food));
        addFoodToDatabase(food);
    }

    public void updateFood(Food food, LinearLayout container) {
        ((TextView) container.findViewById(R.id.foodName)).setText(food.getFoodName());
        ((TextView) container.findViewById(R.id.mealType)).setText(food.getMealType());
        setTextWithPrefix(container.findViewById(R.id.withWho),
                DailyFragment.WITH_WHO_TEXT_PREFIX, food.getWithWho());
        ((TextView) container.findViewById(R.id.sideNotes)).setText(food.getSideNotes());
        ImageView imgView = container.findViewById(R.id.imageContainer);
        Glide.with(getContext()).load(Uri.parse(food.getImageFilePath()))
                .centerCrop()
                .apply(new RequestOptions().override(imgView.getWidth()))
                .into(imgView);

        updateFoodToDatabase(food);
    }

    public static void setTextWithPrefix(TextView textView, String prefix, String suffix) {
        textView.setText(prefix + suffix);
    }

    public void deleteFoodFromDatabase(Food food) {
        DocumentReference userDB = db.document(getDatabasePath());
        Map<String, Object> dataToDelete = new HashMap<String, Object>();
        dataToDelete.put(food.getFoodID(), FieldValue.delete());
        //dataToDelete.put(getImagePathsKey(food), FieldValue.delete());
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
        //dataToAdd.put(getImagePathsKey(food), food.getImageFilePaths());
        dataToAdd.put(food.getFoodID(), food);
        userDB.set(dataToAdd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ViewHelper.getSnackbar(foodCardGridLayout, R.string.add_success_msg,
                                Snackbar.LENGTH_SHORT, floatingBtn).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ViewHelper.getSnackbar(foodCardGridLayout, R.string.add_failed_msg,
                                Snackbar.LENGTH_LONG, floatingBtn).show();
                    }
                });
    }

    public void updateFoodToDatabase(Food food) {
        DocumentReference userDB = db.document(getDatabasePath());
        Map<String, Object> dataToModify = new HashMap<String, Object>();
        dataToModify.put(food.getFoodID(), food);
        //dataToModify.put(getImagePathsKey(food), food.getImageFilePaths());
        userDB.update(dataToModify)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ViewHelper.getSnackbar(foodCardGridLayout, R.string.modify_success_msg,
                                Snackbar.LENGTH_SHORT, floatingBtn).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ViewHelper.getSnackbar(foodCardGridLayout, R.string.modify_failed_msg,
                                Snackbar.LENGTH_LONG, floatingBtn).show();
                    }
                });
    }

    private String getDatabasePath() {
        return DBHelper.joinPath(userDataDir, user.getUid(), foodDir, Time.getDayID(date));
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

    private String getImagePathsKey(Food food) {
        return food.getFoodID() + "Images";
    }
}