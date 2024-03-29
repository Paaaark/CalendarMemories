package com.example.calendarmemories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DailyMemory {

    public static final String NUM_FOOD_KEY = "numFood";
    public static final String ID_TEMPLATE_KEY = "idTemplate";

    private ArrayList<Food> foods;
    private long idTemplate;
    private LocalDate date;

    public DailyMemory() {
        foods = new ArrayList<Food>();
        idTemplate = 1l;
    }

    public DailyMemory(DocumentSnapshot document, LocalDate date) {
        this.date = date;
        foods = new ArrayList<Food>();
        if (document.exists()) {
            Map<String, Object> data = document.getData();
            for (String key : data.keySet()) {
                if (key.equals(ID_TEMPLATE_KEY)) {
                    idTemplate = Long.parseLong(data.get(key).toString());
                } else if (!key.equals(NUM_FOOD_KEY)) {
                    foods.add(new Food((HashMap<String, Object>) data.get(key)));
                }
            }
        } else {
            // #TODO: Document not found response
            System.out.println("Document does not exist");
        }
    }

    @Deprecated
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
        return Time.toString(date) + "Food" + Long.toString(idTemplate++);
    }

    public long getIDTemplate() {
        return idTemplate;
    }
}
