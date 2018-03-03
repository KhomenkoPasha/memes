package com.memes.khom.mnews.viewholder;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.activities.PictureActivity;
import com.memes.khom.mnews.activities.PostDetailActivity;
import com.memes.khom.mnews.activities.StartActivity;
import com.memes.khom.mnews.activities.YouToubeActivity;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;

import java.util.LinkedList;

import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

public class FirebasePostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private LayoutInflater inflater;
    private LinkedList<Post> posts = new LinkedList<>();
    private LinkedList<String> keys = new LinkedList<>();
    private Context cnx;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    public FirebasePostAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        cnx = context;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {

        holder.iv_piture.setImageResource(R.drawable.image_no);
        final Post model = posts.get(position);
        final String postKey = keys.get(position);
        final boolean[] like = {false};
        final int[] likes = {model.likes_count};

        holder.lvHeaderPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PostDetailActivity
                Intent intent = new Intent(cnx, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                cnx.startActivity(intent);
            }
        });


        holder.comments_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PostDetailActivity
                Intent intent = new Intent(cnx, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                cnx.startActivity(intent);
            }
        });

        if (model.postType == 0) {

            mStorageRef.child("images/" + postKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    GlideApp.with(cnx)
                            .load(uri)
                            //.apply(centerCropTransform())
                            // .centerCrop()
                            //  .placeholder(R.drawable.image_no).crossFade()
                            .into(holder.iv_piture);

                    holder.iv_piture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (cnx != null) {
                                Intent myIntent = new Intent(cnx, PictureActivity.class);
                                myIntent.putExtra("photo_url", uri);
                                cnx.startActivity(myIntent);
                            }
                        }
                    });
                }
            });
        }
        //video
        else {
            holder.iv_piture.setVisibility(View.GONE);

            final YouTubeThumbnailLoader.OnThumbnailLoadedListener  onThumbnailLoadedListener = new YouTubeThumbnailLoader.OnThumbnailLoadedListener(){
                @Override
                public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                }

                @Override
                public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                    youTubeThumbnailView.setVisibility(View.VISIBLE);
                    holder.youtube.setVisibility(View.VISIBLE);
                }
            };


            holder.youtube_thumbnail.initialize("44267967193-5tr8kfuangbssq4sf5dkdmimfogis72t.apps.googleusercontent.com", new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    youTubeThumbnailLoader.setVideo(model.videoid);
                    youTubeThumbnailLoader.setOnThumbnailLoadedListener(onThumbnailLoadedListener);
                    holder.youtube.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(cnx, YouToubeActivity.class);
                            intent.putExtra(YouToubeActivity.VIDEOTAG, model.videoid);
                            cnx.startActivity(intent);
                        }
                    });
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                    //write something for failure
                }
            });

        }

        FirebaseDatabase.getInstance().getReference().child("users/" + model.uid + "/uriPhoto")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            String str = snapshot.getValue().toString();
                            if (str != null && !str.isEmpty()) {
                                GlideApp.with(cnx)
                                        .load(str)
                                        .into(holder.post_author_photo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        // Determine if the current user has liked this post and set UI accordingly
        if (model.likes.containsKey(getUid())) {
            like[0] = true;
            holder.likeView.setImageResource(R.drawable.ic_toggle_star_24);

        } else {
            like[0] = false;
            holder.likeView.setImageResource(R.drawable.ic_toggle_star_outline_24);
        }
        // Bind Post to ViewHolder, setting OnClickListener for the star button
        holder.bindToPost(model, new View.OnClickListener() {
            @Override
            public void onClick(View starView) {
                // Need to write to both places the post is stored
                DatabaseReference globalPostRef = mDatabase.child("posts").child(postKey);
                DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postKey);
                onLikeClicked(globalPostRef);
                onLikeClicked(userPostRef);
                if (like[0]) {
                    holder.likeView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    like[0] = false;
                    likes[0]--;
                    holder.numStarsView.setText(String.valueOf(likes[0]));
                } else {
                    likes[0]++;
                    holder.likeView.setImageResource(R.drawable.ic_toggle_star_24);
                    holder.numStarsView.setText(String.valueOf(likes[0]));
                    like[0] = true;
                }

            }
        }, cnx);

    }


    // [START post_stars_transaction]
    private void onLikeClicked(DatabaseReference postRef) {
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
                // Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public LinkedList<Post> getPosts() {
        return posts;
    }

    /*
        @Override
        public int getItemViewType(int position) {
            return posts.get(position).hashCode();
        }
    */
    @Override
    public long getItemId(int position) {
        return position;
    }


    public LinkedList<String> getKeys() {
        return keys;
    }

}
