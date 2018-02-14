package com.memes.khom.mnews.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopPostsFragment extends PostListFragment {

    public MyTopPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        String myUserId = getUid();
        return databaseReference.child("user-posts").child(myUserId)
                .orderByChild("likes_count").limitToFirst(10);
    }
}
