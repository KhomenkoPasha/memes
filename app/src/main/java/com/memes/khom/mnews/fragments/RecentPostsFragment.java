package com.memes.khom.mnews.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("posts").orderByChild("category").equalTo("kiks")
                .limitToFirst(100);
        return recentPostsQuery;
    }
}
