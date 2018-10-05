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

import java.util.Objects;


public abstract class PostListFragment extends Fragment  {

    private RecyclerView mRecycler;
    private FirebasePostAdapter fbadapt;
   // private SwipeRefreshLayout mSwipeRefreshLayout;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        try {
            mRecycler = rootView.findViewById(R.id.messages_list);
            // mRecycler.setHasFixedSize(true);
          //  mSwipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
          //  mSwipeRefreshLayout.setOnRefreshListener(this);
         //   mSwipeRefreshLayout.setColorScheme(R.color.accent, R.color.accent, R.color.accent, R.color.accent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rootView;
    }


    public void refreshFragment(Query postsQuery) {

        fbadapt = new FirebasePostAdapter(getContext());
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //  try {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    if (!fbadapt.getKeys().contains(dataSnapshot.getKey())) {
                        fbadapt.getPosts().addFirst(post);
                        fbadapt.getKeys().addFirst(dataSnapshot.getKey());
                        fbadapt.notifyItemInserted(fbadapt.getItemCount());
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

    /*
    @Override
    public void onRefresh() {
        // говорим о том, что собираемся начать
        // Toast.makeText(getActivity(), "start refresh", Toast.LENGTH_SHORT).show();
        // начинаем показывать прогресс
        mSwipeRefreshLayout.setRefreshing(true);
        refreshFragment(getQuery(FirebaseDatabase.getInstance().getReference()));
        // ждем 3 секунды и прячем прогресс
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                // Toast.makeText(getActivity(), "finish refresh", Toast.LENGTH_SHORT).show();
            }
        }, 3000);
    }
*/
    public String getUid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }


    public void loadNextPostsLikesCount(int page) {

        int LikeCount = fbadapt.getPosts().get(fbadapt.getItemCount() - 1).likes_count;

        Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
                .orderByChild("likes_count").startAt(LikeCount - 10).endAt(LikeCount + 10).limitToFirst(5 * page);


        Log.d("LikeCount", String.valueOf(LikeCount));
        Log.d("LikeCountAD", String.valueOf(fbadapt.getItemCount()));

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    if (!fbadapt.getKeys().contains(dataSnapshot.getKey())) {
                        fbadapt.getPosts().addLast(post);
                        fbadapt.getKeys().addLast(dataSnapshot.getKey());
                        fbadapt.notifyItemInserted(fbadapt.getItemCount());
                        Log.d("post_likes", String.valueOf(post.likes_count));
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
        imagesQuery.addChildEventListener(childEventListener);

    }


    public void loadNextPostsOldest(int page) {

        // Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
        //         .orderByChild("likes_count").startAt(LikeCount - 1).endAt(LikeCount + 1).limitToFirst(5*page);

        long create_date = fbadapt.getPosts().get(fbadapt.getItemCount() - 1).create_date;
        Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
                .orderByChild("create_date").startAt(create_date - page * 106400000).endAt(create_date)
                .limitToLast(page * 7);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    if (!fbadapt.getKeys().contains(dataSnapshot.getKey())) {
                        fbadapt.getPosts().addLast(post);
                        fbadapt.getKeys().addLast(dataSnapshot.getKey());
                        fbadapt.notifyItemInserted(fbadapt.getItemCount());
                        Log.d("create_date", "");
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
        imagesQuery.addChildEventListener(childEventListener);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mManager) {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (PostListFragment.this instanceof AllTopPostsFragment) {
                    loadNextPostsLikesCount(page);
                }

                if (PostListFragment.this instanceof RecentPostsFragment) {
                    loadNextPostsOldest(page);
                }

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
