package com.memes.khom.mnews.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.viewholder.EndlessRecyclerViewScrollListener;
import com.memes.khom.mnews.viewholder.FirebasePostAdapter;


public abstract class PostListFragment extends Fragment {

    private RecyclerView mRecycler;
    private FirebasePostAdapter fbadapt;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        mRecycler = rootView.findViewById(R.id.messages_list);
        //mRecycler.setHasFixedSize(true);
        return rootView;
    }


    public void refreshFragment(Query postsQuery) {

        fbadapt = new FirebasePostAdapter(getContext());
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    fbadapt.getPosts().add(post);
                    fbadapt.getKeys().add(dataSnapshot.getKey());
                    fbadapt.notifyItemInserted(fbadapt.getItemCount());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    int pos = fbadapt.getKeys().indexOf(dataSnapshot.getKey());
                    fbadapt.getPosts().set(pos, post);
                    fbadapt.notifyItemChanged(pos);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        postsQuery.addChildEventListener(childEventListener);
        mRecycler.setAdapter(fbadapt);
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public void loadNextPosts(int page) {

        int starCount = fbadapt.getPosts().get(fbadapt.getItemCount() - 1).likes_count;
        // int i = mAdapter.getItemCount(); getReference().child("posts").orderByChild("title").startAt(query)
        //  .endAt(query + "\uf8ff")
        //  .limitToFirst(50)
        Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
                .orderByChild("likes_count").endAt(starCount - 1).limitToLast(page * 2);


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        fbadapt.getPosts().add(post);
                        fbadapt.getKeys().add(dataSnapshot.getKey());
                        fbadapt.notifyItemInserted(fbadapt.getItemCount());
                        Log.d("post_likes", String.valueOf(post.likes_count));
                    }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        imagesQuery.addChildEventListener(childEventListener);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
         //mManager.setReverseLayout(true);
         //mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mManager) {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                loadNextPosts(page);
                return false;
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecycler.addOnScrollListener(scrollListener);
        refreshFragment(getQuery(FirebaseDatabase.getInstance().getReference()));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public abstract Query getQuery(DatabaseReference databaseReference);
}
