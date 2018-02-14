package com.memes.khom.mnews.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.memes.khom.mnews.activities.PictureActivity;
import com.memes.khom.mnews.activities.PostDetailActivity;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.models.Post;
import com.memes.khom.mnews.utils.ImageUtils;
import com.memes.khom.mnews.viewholder.EndlessRecyclerViewScrollListener;
import com.memes.khom.mnews.viewholder.EndlessScrollListener;
import com.memes.khom.mnews.viewholder.PostViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int fragment = 0;

    public FirebaseRecyclerAdapter<Post, PostViewHolder> getmAdapter() {
        return mAdapter;
    }

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mRecycler = rootView.findViewById(R.id.messages_list);
        //mRecycler.setHasFixedSize(true);
        return rootView;
    }

    public void loadNextDataFromApi(int offset, int totalItemsCount) {
        Log.d("page - ", String.valueOf(offset));
        //  Toast.makeText(getActivity(),String.valueOf(offset),Toast.LENGTH_LONG).show();
    }

    public void refreshFragment(Query postsQuery) {

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);
                // Set click listener for the whole post view
                final String postKey = postRef.getKey();


                viewHolder.lvHeaderPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                viewHolder.comments_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                mStorageRef.child("images/" + postKey).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Target target = new Target() {

                                    @Override
                                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                        Bitmap originalPhoto = ImageUtils.getResizeFile(bitmap, viewHolder.itemView.getWidth());
                                        viewHolder.iv_piture.setImageBitmap(originalPhoto);
                                        viewHolder.iv_piture.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (getContext() != null) {
                                                    Intent myIntent = new Intent(getContext(), PictureActivity.class);
                                                    myIntent.putExtra("photo_url", uri);
                                                    getContext().startActivity(myIntent);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    }
                                };
                                Picasso.with(getContext())
                                        .load(uri)
                                        .into(target);
                                viewHolder.iv_piture.setTag(target);
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Load", "" + e);

                    }
                });


                FirebaseDatabase.getInstance().getReference().child("users/" + model.uid + "/uriPhoto")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    String str = snapshot.getValue().toString();
                                    if (str != null && !str.isEmpty()) {
                                        Picasso.with(getContext()).load(str).into(viewHolder.post_author_photo);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.likes.containsKey(getUid())) {
                    viewHolder.likeView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.likeView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                }, getContext());
            }
        };
        mRecycler.setAdapter(mAdapter);

    }


    public void loadNextPosts() {

        int starCount = mAdapter.getItem(mAdapter.getItemCount() - 1).likes_count;
        if (fragment == 0) {
            // int i = mAdapter.getItemCount(); getReference().child("posts").orderByChild("title").startAt(query)
            //  .endAt(query + "\uf8ff")
            //  .limitToFirst(50)
            Query imagesQuery = FirebaseDatabase.getInstance().getReference().child("posts")
                    .orderByChild("likes_count").endAt(starCount - 1);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
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
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
        // mManager.setReverseLayout(true);
        // mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        scrollListener = new EndlessRecyclerViewScrollListener(mManager) {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                loadNextPosts();
                return false;
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecycler.addOnScrollListener(scrollListener);


        refreshFragment(getQuery(mDatabase));
    }


    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null)
                    return Transaction.success(mutableData);

                if (p.likes.containsKey(getUid())) {
                    p.likes_count = p.likes_count - 1;
                    p.likes.remove(getUid());
                } else {
                    p.likes_count = p.likes_count + 1;
                    p.likes.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    // public abstract RecyclerView getQuery(RecyclerView recyclerView);
}
