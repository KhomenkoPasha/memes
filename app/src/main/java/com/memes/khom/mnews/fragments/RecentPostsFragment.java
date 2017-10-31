package com.memes.khom.mnews.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("posts").orderByChild("create_date")
                .limitToLast(50);
    }
}
