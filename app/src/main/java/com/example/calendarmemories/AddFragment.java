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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import com.example.calendarmemories.databinding.FragmentAddBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

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

    public static String TAG = "AddDialogFragment";
    private static final String IMAGE_PICKER_TITLE = "Select an image";

    public AddFragment() {
        super(R.layout.fragment_add);
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

        // #TODO: Add button actions
        addConfirmBtn = getView().findViewById(R.id.addConfirmBtn);
        addConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String foodName = foodInputText.getText().toString();
                String mealType = mealTypeInput.getText().toString();
                String withWho = withWhoInputText.getText().toString();
                String sideNotes = sideNotesInputText.getText().toString();
                Food food = new Food(imgUri, foodName, mealType, withWho, sideNotes);
                System.out.println("*****Parent: " + getParentFragment());
                ((DailyFragment) getParentFragment()).addFood(food);
                dismiss();
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
}