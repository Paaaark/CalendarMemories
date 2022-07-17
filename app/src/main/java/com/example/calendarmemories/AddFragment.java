package com.example.calendarmemories;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AddFragment extends DialogFragment {

    private Button addConfirmBtn, cancelBtn, addImgBtn;
    private TextInputEditText foodInputText, withWhoInputText, sideNotesInputText;
    private TextView todaysDate;
    private CircularProgressIndicator circularProgressIndicator;
    private AutoCompleteTextView mealTypeInput;
    private ImageView foodImgView;
    private Uri imgUri = null;
    private boolean editingFood = false;
    private boolean imgViewFlag = true;
    private boolean fromCalendar = false;
    private LinearLayout container;
    private Food food;
    private String foodID;
    private LocalDate date;

    public static String TAG = "AddDialogFragment";
    private static final String IMAGE_PICKER_TITLE = "Select an image";
    private static final String DATE_PICKER_TITLE = "Select date";
    private static final String EMPTY_STRING = "";
    private static final int TAKE_PICTURE = 0;
    private static final int SELECT_PICTURE = 1;
    private static final String MIME_TYPE = "image/png";
    private static final int HEIGHT = 0;
    private static final int WIDTH = 0;

    public AddFragment(String foodID) {
        super(R.layout.fragment_add);
        this.foodID = foodID;
    }

    public AddFragment(Food food, LinearLayout container, String foodID) {
        super(R.layout.fragment_add);
        this.food = food;
        this.container = container;
        this.foodID = foodID;
        food.setFoodID(foodID);
        editingFood = true;
    }

    public AddFragment(LocalDate date) {
        super(R.layout.fragment_add);
        this.date = date;
        this.fromCalendar = true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // To listen to back button
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();

        // Set the size of the dialog to match the parent
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Load meal types from res and load it into the dropdown
        String[] mealTypes = getResources().getStringArray(R.array.mealTypesArray);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.meal_type_dropdown_items, mealTypes);
        AutoCompleteTextView autoCompleteTextView = getView().findViewById(R.id.autoCompleteMealType);
        autoCompleteTextView.setAdapter(arrayAdapter);

        // Initializing TextInputEditText, AutoCompleteTextView, and ImageView
        foodInputText = getView().findViewById(R.id.foodInputText);
        withWhoInputText = getView().findViewById(R.id.withWhoInputText);
        sideNotesInputText = getView().findViewById(R.id.sideNotesInputText);
        mealTypeInput = getView().findViewById(R.id.autoCompleteMealType);
        foodImgView = getView().findViewById(R.id.foodImgView);
        circularProgressIndicator = getView().findViewById(R.id.circularProgressIndicator);
        todaysDate = getView().findViewById(R.id.todaysDate);

        // If we got here from calendarView, add a date picker
        if (fromCalendar) {
            todaysDate.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            todaysDate.setText(Time.toString(date));
            todaysDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MaterialDatePicker picker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText(DATE_PICKER_TITLE)
                            .setSelection(Time.inMillis(date))
                            .setCalendarConstraints(Time.getCalendarConstraints(date))
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
                            date = newDate;
                        }
                    });
                }
            });
        }

        // If we are in editing mode, initialize the texts into the textViews
        if (editingFood) {
            ViewTreeObserver viewTreeObserver = foodImgView.getViewTreeObserver();
            viewTreeObserver
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (food.getImageFilePath() != null && imgViewFlag) {
                                putImageInView(food.getImageFilePath(), foodImgView.getWidth(), foodImgView);
                                imgViewFlag = false;
                            }
                            foodImgView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
            foodInputText.setText(food.getFoodName());
            withWhoInputText.setText(food.getWithWho());
            sideNotesInputText.setText(food.getSideNotes());
            mealTypeInput.setText(food.getMealType(), false);
        }

        // #TODO: Add button actions
        addConfirmBtn = getView().findViewById(R.id.addConfirmBtn);
        addConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Adding a new entry of food
                if (!editingFood) {
                    String foodName = foodInputText.getText().toString();
                    String mealType = mealTypeInput.getText().toString();
                    String withWho = withWhoInputText.getText().toString();
                    String sideNotes = sideNotesInputText.getText().toString();
                    Food food;
                    if (imgUri == null) {
                        food = new Food(null, foodName, mealType, withWho, sideNotes);
                    } else {
                        food = new Food(imgUri.toString(), foodName, mealType, withWho, sideNotes);
                    }
                    food.setFoodID(foodID);
                    System.out.println("*****Parent: " + getParentFragment());
                    if (getParentFragment() instanceof DailyFragment) {
                        ((DailyFragment) getParentFragment()).addFood(food);
                    } else if (getParentFragment() instanceof CalendarFragment) {
                        // #TODO: Calendar fragment add food
                        ((CalendarFragment) getParentFragment()).addFood(food, date);
                    }
                } else {
                    // Modifying an existing entry
                    if (imgUri != null) {
                        food.setImageFilePath(imgUri.toString());
                    }
                    food.setFoodName(foodInputText.getText().toString());
                    food.setMealType(mealTypeInput.getText().toString());
                    food.setWithWho(withWhoInputText.getText().toString());
                    food.setSideNotes(sideNotesInputText.getText().toString());
                    ((TextView) container.findViewById(R.id.listCardFoodNameTxt)).setText(food.getFoodName());
                    ((TextView) container.findViewById(R.id.listCardMealTypeTxt)).setText(food.getMealType());
                    DailyFragment.setTextWithPrefix(container.findViewById(R.id.listCardWithWhoTxt),
                            DailyFragment.WITH_WHO_TEXT_PREFIX, food.getWithWho());
                    ((TextView) container.findViewById(R.id.listCardSideNotesTxt)).setText(food.getSideNotes());
                    ImageView imageView = container.findViewById(R.id.listCardImageContainer);
                    putImageInView(food.getImageFilePath(), imageView.getWidth(), imageView);
                    ((DailyFragment) getParentFragment()).updateFoodToDatabase(food);
                }
                dismiss();
            }
        });
        // When cancelBtn is clicked, dismiss the dialog
        cancelBtn = getView().findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changesMade()) {
                    getAlertDialogBuilder(R.string.back_button_alert_title,
                            R.string.back_button_alert_msg)
                            .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dismiss();
                                }
                            })
                            .setNegativeButton(R.string.continue_editing, null)
                            .show();
                } else {
                    dismiss();
                }
            }
        });
        // #TODO: addImgBtn logic
        addImgBtn = getView().findViewById(R.id.addImgBtn);
        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTwoOptionsAlertDialogBuilder();
            }
        });
        foodImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTwoOptionsAlertDialogBuilder();
            }
        });

        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (changesMade()) {
                        getAlertDialogBuilder(R.string.back_button_alert_title,
                                R.string.back_button_alert_msg)
                                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dismiss();
                                    }
                                })
                                .setNegativeButton(R.string.continue_editing, null)
                                .show();
                    } else {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void imageSelector() {
        imgUri = getImgUri();
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        launchImagePicker.launch(takePicture);
    }

    public void imageChooser() {
        Intent imagePicker = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_OPEN_DOCUMENT);
        launchImagePicker.launch(imagePicker);
    }

    ActivityResultLauncher<Intent> launchImagePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent == null) {
                            Uri selectedImageUri = imgUri;
                            System.out.println("Uri: " + selectedImageUri.toString());
                            int width = foodImgView.getWidth();
                            putImageInView(selectedImageUri, width, foodImgView);
                        } else if (intent != null && intent.getData() != null) {
                            Uri selectedImageUri = intent.getData();
                            System.out.println("Selected img uri path: " + selectedImageUri.getPath());
                            int width = foodImgView.getWidth();
                            putImageInView(selectedImageUri, width, foodImgView);
                            // #TODO: Food image icon to have a certain color
                            imgUri = selectedImageUri;
                        }
                    }
                }
            });

    private int getImageHeight(int width) {
        return (int) (width * 9.0 / 16.0);
    }

    private void putImageInView(String imageFilePath, int maxSide, ImageView view) {
        if (imageFilePath == null) return;
        Uri imgUri = Uri.parse(imageFilePath);
        putImageInView(imgUri, maxSide, view);
    }

    private void putImageInView(Uri imgUri, int maxSide, ImageView view) {
        if (imgUri == null) return;
        int size[] = getImgSize(imgUri, maxSide);
        Glide.with(getContext()).load(imgUri)
                .centerCrop()
                .apply(new RequestOptions().override(size[WIDTH], size[HEIGHT]))
                .into(view);
    }

    private int[] getImgSize(Uri imageUri, int maxSide) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        ParcelFileDescriptor fd = null;
        try {
            fd = getContext().getContentResolver().openFileDescriptor(imageUri, "r");
        } catch (FileNotFoundException e) {
            return new int[2];
        }

        BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
        int[] size = { options.outHeight, options.outWidth };
        if (size[HEIGHT] > size[WIDTH]) {
            double multiplier = size[HEIGHT] / size[WIDTH];
            size[WIDTH] = maxSide;
            size[HEIGHT] = (int) (maxSide * multiplier);
        } else {
            double multiplier = size[WIDTH] / size[HEIGHT];
            size[HEIGHT] = maxSide;
            size[WIDTH] = (int) (maxSide * multiplier);
        }
        return size;
    }

    private MaterialAlertDialogBuilder getAlertDialogBuilder(int titleRes, int msgRes) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(titleRes)
                .setMessage(msgRes);
        return builder;
    }

    private void showTwoOptionsAlertDialogBuilder() {
        // #TODO: String array adapting
        String[] takeOrSelect = getResources().getStringArray(R.array.takeOrSelect);
        new MaterialAlertDialogBuilder(getContext())
            .setItems(takeOrSelect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case TAKE_PICTURE:
                            imageSelector();
                            break;
                        case SELECT_PICTURE:
                            imageChooser();
                            break;
                    }
                }
            }).show();
    }

    private boolean changesMade() {
        if (editingFood) {
            if (!food.getFoodName().equals(foodInputText.getText().toString()) ||
                    !food.getMealType().equals(mealTypeInput.getText().toString()) ||
                    !food.getWithWho().equals(withWhoInputText.getText().toString()) ||
                    !food.getSideNotes().equals(sideNotesInputText.getText().toString()) ||
                    !food.getImageFilePath().equals(imgUri.toString())) {
                return true;
            }
        } else {
            if (!foodInputText.getText().toString().equals(EMPTY_STRING) ||
                !mealTypeInput.getText().toString().equals(EMPTY_STRING) ||
                !withWhoInputText.getText().toString().equals(EMPTY_STRING) ||
                !sideNotesInputText.getText().toString().equals(EMPTY_STRING) ||
                imgUri != null) {
                return true;
            }
        }
        return false;
    }

    private Uri getImgUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, foodID);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE);
            contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + foodID);
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            File imagesDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + foodID);
            return Uri.fromFile(imagesDir);
        }
    }
}