package com.memes.khom.mnews.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AllTopPostsFragment extends PostListFragment {

    public AllTopPostsFragment() {}


    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("posts")
                .orderByChild("likes_count").limitToFirst(2);
    }
}
