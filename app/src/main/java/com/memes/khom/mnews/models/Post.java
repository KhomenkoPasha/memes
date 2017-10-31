package com.memes.khom.mnews.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public String create_date;
    public String category;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body,String cat) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.category = cat;
        this.create_date = String.valueOf(System.currentTimeMillis());
                //new SimpleDateFormat("dd.MM.yyyy hh:mm").format(new Date());
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("create_date", create_date);
        result.put("category", category);
        return result;
    }
    // [END post_to_map]

}
// [END post_class]
