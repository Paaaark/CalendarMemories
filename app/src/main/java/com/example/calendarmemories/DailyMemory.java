package com.example.calendarmemories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DailyMemory {
    private ArrayList<Food> foods;
    private int idTemplate;

    public DailyMemory() {
        foods = new ArrayList<Food>();
        idTemplate = 1;
    }

    public DailyMemory(DocumentSnapshot document) {
        foods = new ArrayList<Food>();
        if (document.exists()) {
            Map<String, Object> data = document.getData();
            for (String key : data.keySet()) {
                if (!key.equals("numFood")) {
                    foods.add(new Food((HashMap<String, Object>) data.get(key)));
                }
            }
        } else {
            // #TODO: Document not found response
            System.out.println("Document does not exist");
        }
    }

    public DailyMemory(ObjectInputStream objIn) {
        foods = new ArrayList<Food>();
        int numObjs = 0;
        try {
            numObjs = objIn.readInt();
        } catch (IOException e) {
            numObjs = -1;
        }
        for (int i = 0; i < numObjs; i++) {
            try {
                Food food = (Food) objIn.readObject();
                foods.add(food);
            } catch (Exception e) {
                // #TODO: Exception handling
            }
        }
    }
    public void updateFile(ObjectOutputStream objOut) {
        try {
            objOut.writeInt(foods.size());
            for (Food f: foods) {
                objOut.writeObject(f);
            }
        } catch (Exception e) {
            // #TODO: Exception handling
        }
    }

    public void addFood(Food f) {
        foods.add(f);
    }

    public Food getFoodAt(int index) {
        if (index >= foods.size()) {
            return null;
        } else {
            return foods.get(index);
        }
    }

    public int getNumFood() {
        return foods.size();
    }

    public String generateFoodID() {
        return "Food" + Integer.toString(idTemplate++);
    }
}
