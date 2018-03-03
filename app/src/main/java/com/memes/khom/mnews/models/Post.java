package com.memes.khom.mnews.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public long create_date;
    public String category;
    public int likes_count = 0;
    public int postType = 0;
    public String videoid;
    public Map<String, Boolean> likes = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body, String cat, int postType,String videoid) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.category = cat;
        this.create_date = System.currentTimeMillis();
        this.postType = postType;
        this.videoid = videoid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", likes_count);
        result.put("stars", likes);
        result.put("create_date", create_date);
        result.put("category", category);
        result.put("searcher", title + body + category);
        result.put("postType", postType);
        result.put("videoid", videoid);
        return result;
    }

}
