package com.memes.khom.mnews.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String uriPhoto;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String urPhoto) {
        this.username = username;
        this.email = email;
        this.uriPhoto = urPhoto;
    }

}
// [END blog_user_class]
