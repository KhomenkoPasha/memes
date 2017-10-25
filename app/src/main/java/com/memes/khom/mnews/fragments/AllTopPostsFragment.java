package com.memes.khom.mnews.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class AllTopPostsFragment extends PostListFragment {

    public AllTopPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("posts").orderByChild("starCount")
                .limitToFirst(100);
        return recentPostsQuery;
    }
}
