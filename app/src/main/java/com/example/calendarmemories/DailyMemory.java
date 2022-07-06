package com.example.calendarmemories;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DailyMemory {
    private ArrayList<Food> foods;

    public DailyMemory() {
        foods = new ArrayList<Food>();
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
}
