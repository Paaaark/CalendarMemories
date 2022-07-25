package com.example.calendarmemories;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

public class EditPictureFragment extends Fragment {

    private Uri imageUri;
    private boolean isFavorite;
    private MaterialButton starBtn, deleteBtn;
    private View v;

    public EditPictureFragment() {
        // Required empty public constructor
    }

    public EditPictureFragment(Uri imageUri, boolean isFavorite) {
        this.imageUri = imageUri;
        this.isFavorite = isFavorite;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_edit_picture, container, false);

        starBtn = v.findViewById(R.id.pictureStarBtn);
        deleteBtn = v.findViewById(R.id.pictureDeleteBtn);

        if (isFavorite) {
            starBtn.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_star_filled));
        }

        starBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavorite) {

                }
            }
        });
        return v;
    }
}