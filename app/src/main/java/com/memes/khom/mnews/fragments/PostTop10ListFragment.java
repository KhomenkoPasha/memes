package com.memes.khom.mnews.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.viewholder.FirebasePostTop10Adapter;

import java.util.Calendar;
import java.util.Objects;


public class PostTop10ListFragment extends Fragment {

    private RecyclerView mRecycler;
    private FirebasePostTop10Adapter fbadapt;

    public PostTop10ListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        try {
            mRecycler = rootView.findViewById(R.id.messages_list);

            // LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            // mLayoutManager.setReverseLayout(false);
            // mLayoutManager.setStackFromEnd(false);

            //  mRecycler.setLayoutManager(mLayoutManager);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rootView;
    }


    public void refreshFragment(Query postsQuery) {

        fbadapt = new FirebasePostTop10Adapter(getContext());
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    if (!fbadapt.getKeys().contains(dataSnapshot.getKey())) {
                        post.keyRec = dataSnapshot.getKey();
                        fbadapt.getPosts().addFirst(post);
                        fbadapt.getKeys().addFirst(dataSnapshot.getKey());
                        fbadapt.sort();
                        //  fbadapt.notifyItemInserted(fbadapt.getItemCount());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        postsQuery.addChildEventListener(childEventListener);
        mRecycler.setAdapter(fbadapt);
    }


    public String getUid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Query q = FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("create_date")
                .startAt(c.getTimeInMillis()).endAt(System.currentTimeMillis())
                .limitToFirst(10);
        refreshFragment(q);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
