package com.example.calendarmemories;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.calendarmemories.databinding.FragmentAddBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AddFragment extends DialogFragment {

    private Button addConfirmBtn, cancelBtn, addImgBtn;
    private TextInputEditText foodInputText, withWhoInputText, sideNotesInputText;
    private AutoCompleteTextView mealTypeInput;
    private ImageView foodImgView;
    private Uri imgUri = null;
    private boolean editingFood = false;
    private LinearLayout container;
    private Food food;
    private boolean imgViewFlag = true;

    public static String TAG = "AddDialogFragment";
    private static final String IMAGE_PICKER_TITLE = "Select an image";

    public AddFragment() {
        super(R.layout.fragment_add);
    }

    public AddFragment(Food food, LinearLayout container) {
        super(R.layout.fragment_add);
        this.food = food;
        this.container = container;
        editingFood = true;
    }

    @Override
    public void onStart() {
        super.onStart();

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
        foodImgView.setColorFilter(R.color.minimal_pink_light);

        // If we are in editing mode, initialize the texts into the textViews
        if (editingFood) {
            ViewTreeObserver viewTreeObserver = foodImgView.getViewTreeObserver();
            viewTreeObserver
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (food.getImageFilePath() != null && imgViewFlag) {
                                putImageInView(food.getImageFilePath(), foodImgView.getWidth(),
                                        getImageHeight(foodImgView.getHeight()), foodImgView);
                                imgViewFlag = false;
                            }
                            foodImgView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
            foodInputText.setText(food.getFoodName());
            withWhoInputText.setText(food.getWithWho());
            sideNotesInputText.setText(food.getSideNotes());
            mealTypeInput.setText(food.getMealType());
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
                    Food food = new Food(imgUri, foodName, mealType, withWho, sideNotes);
                    System.out.println("*****Parent: " + getParentFragment());
                    ((DailyFragment) getParentFragment()).addFood(food);
                } else {
                    // Modifying an existing entry
                    food.setImageUri(imgUri);
                    DailyFragment.saveFoodImg(getContext(), food.getImageFilePath(), food.getImageUri());
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
                    putImageInView(food.getImageUri(), imageView.getWidth(), imageView.getHeight(),
                            imageView);
                    ((DailyFragment) getParentFragment()).updateFoodToDatabase(food);
                }
                dismiss();;
            }
        });
        // When cancelBtn is clicked, dismiss the dialog
        cancelBtn = getView().findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        // #TODO: addImgBtn logic
        addImgBtn = getView().findViewById(R.id.addImgBtn);
        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });
        foodImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });
    }

    public void imageChooser() {
        Intent imagePicker = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_OPEN_DOCUMENT);
        launchImagePicker.launch(imagePicker);
    }

    ActivityResultLauncher<Intent> launchImagePicker
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        int width = foodImgView.getWidth();
                        Picasso.get().load(selectedImageUri)
                                .resize(width, getImageHeight(width))
                                .centerCrop()
                                .into(foodImgView);
                        // #TODO: Food image icon to have a certain color
                        foodImgView.clearColorFilter();
                        imgUri = selectedImageUri;
                    }
                }
            });

    private int getImageHeight(int width) {
        return (int) (width * 9.0 / 16.0);
    }

    private void putImageInView(String imageFilePath, int width, int height, ImageView view) {
        File file = new File(getContext().getFilesDir(), imageFilePath);
        Picasso.get().load(file)
                .resize(width, height)
                .centerCrop()
                .into(view);
    }

    private void putImageInView(Uri imageUri, int width, int height, ImageView view) {
        Picasso.get().load(imageUri)
                .resize(width, height)
                .centerCrop()
                .into(view);
    }
}