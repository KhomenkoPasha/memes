package com.memes.khom.mnews.models;


import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Category {

    public String name;


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        return result;
    }
}
