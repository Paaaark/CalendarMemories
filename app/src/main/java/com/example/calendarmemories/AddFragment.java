package com.example.calendarmemories;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.calendarmemories.databinding.FragmentAddBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AddFragment extends DialogFragment {

    private Button addConfirmBtn, cancelBtn;
    private TextInputEditText foodInputText, withWhoInputText, sideNotesInputText;
    private AutoCompleteTextView mealTypeInput;

    public static String TAG = "AddDialogFragment";

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

        // Initializing TextInputEditText and AutoCompleteTextView
        foodInputText = getView().findViewById(R.id.foodInputText);
        withWhoInputText = getView().findViewById(R.id.withWhoInputText);
        sideNotesInputText = getView().findViewById(R.id.sideNotesInputText);
        mealTypeInput = getView().findViewById(R.id.autoCompleteMealType);

        // #TODO: Add button actions
        addConfirmBtn = getView().findViewById(R.id.addConfirmBtn);
        addConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String foodName = foodInputText.getText().toString();
                String mealType = mealTypeInput.getText().toString();
                String withWho = withWhoInputText.getText().toString();
                String sideNotes = sideNotesInputText.getText().toString();
                Food food = new Food(null, foodName, mealType, withWho, sideNotes);
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
    }
}