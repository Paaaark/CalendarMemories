package com.example.calendarmemories;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class PicturesFragment extends DialogFragment {

    public static final String TAG = "PicturesFragment";

    private Food food;
    private int picturesWidth = 0;
    private View v;
    private GridLayout picturesGrid;
    private ImageView parentImgView;

    public PicturesFragment() {
        super(R.layout.fragment_pictures);
    }

    public PicturesFragment(Food food, ImageView parentImgView) {
        this.food = food;
        this.parentImgView = parentImgView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_pictures, container, false);

        picturesGrid = v.findViewById(R.id.picturesGrid);


        Dialog dialog = getDialog();
        int windowWidth = (int) (getResources().getDisplayMetrics().widthPixels);
        if (picturesWidth == 0 || windowWidth / picturesWidth == 0) {
            picturesGrid.setColumnCount(3);
            picturesWidth = windowWidth / 3;
            windowWidth = picturesWidth * 3;
        } else {
            picturesGrid.setColumnCount(windowWidth / picturesWidth);
            windowWidth = picturesGrid.getColumnCount() * picturesWidth;
        }
        if (dialog != null) {
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(windowWidth, height);
        }

        ArrayList<String> images = food.getImageFilePaths();
        for (String path: images) {
            putImageInView(path);
        }

        return v;
    }

    private void putImageInView(String filePath) {
        Uri imgUri = Uri.parse(filePath);
        ImageView imgView = new ImageView(getContext());
        imgView.setLayoutParams(new ViewGroup.LayoutParams(picturesWidth, picturesWidth));
        imgView.setPadding(2, 2, 2, 2);
        Glide.with(getContext()).load(imgUri)
                .centerCrop()
                .apply(new RequestOptions().override(picturesWidth))
                .into(imgView);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getContext()).load(imgUri)
                        .centerCrop()
                        .apply(new RequestOptions().override(parentImgView.getHeight()))
                        .into(parentImgView);
                dismiss();
            }
        });

        picturesGrid.addView(imgView);
    }
}