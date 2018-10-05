package com.memes.khom.mnews.viewholder;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.abdularis.buttonprogress.DownloadButtonProgress;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;
import com.memes.khom.mnews.R;
import com.memes.khom.mnews.activities.PictureActivity;
import com.memes.khom.mnews.activities.PostDetailActivity;
import com.memes.khom.mnews.models.GlideApp;
import com.memes.khom.mnews.models.Post;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

public class FirebasePostTop10Adapter extends RecyclerView.Adapter<PosTopViewHolder> {

    private LayoutInflater inflater;
    private LinkedList<Post> posts = new LinkedList<>();
    private LinkedList<String> keys = new LinkedList<>();
    private Context cnx;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    public FirebasePostTop10Adapter(Context context) {
        inflater = LayoutInflater.from(context);
        cnx = context;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sort() {

        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post, Post t1) {
                return t1.likes_count - post.likes_count;
            }
        });

        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public PosTopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_post_top, parent, false);
        return new PosTopViewHolder(view);
    }


    private void shareImage(Uri uriPhoto) {
        try {

            if (Build.VERSION.SDK_INT >= 23) {
                int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(cnx), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) cnx, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }

            }
            if (uriPhoto != null)
                FileLoader.with(cnx)
                        .load(uriPhoto.toString(), true)
                        .fromDirectory(Environment.DIRECTORY_DOWNLOADS, FileLoader.DIR_EXTERNAL_PUBLIC)
                        .asFile(new FileRequestListener<File>() {
                            @Override
                            public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                                try {
                                    File loadedFile = response.getBody();
                                    boolean ileane = loadedFile.renameTo(new File(loadedFile.getPath() + ".jpg"));
                                    if (ileane) {
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("image/*");

                                        Uri uri = (FileProvider.getUriForFile(cnx,
                                                Objects.requireNonNull(cnx).getPackageName() + ".fileProv",
                                                new File(loadedFile.getPath() + ".jpg")));

                                        share.putExtra(Intent.EXTRA_STREAM, uri);
                                        cnx.startActivity(Intent.createChooser(share, "Share Image!"));
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(FileLoadRequest request, Throwable t) {
                            }
                        });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveMemas(Uri uriPhoto, final DownloadButtonProgress download) {

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(cnx), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) cnx, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }

                if (uriPhoto != null)
                    FileLoader.with(cnx)
                            .load(uriPhoto.toString(), true)
                            .fromDirectory(Environment.DIRECTORY_DOWNLOADS, FileLoader.DIR_EXTERNAL_PUBLIC)
                            .asFile(new FileRequestListener<File>() {
                                @Override
                                public void onLoad(FileLoadRequest request, FileResponse<File> response) {
                                    try {
                                        File loadedFile = response.getBody();
                                        boolean ileane = loadedFile.renameTo(new File(loadedFile.getPath() + ".jpg"));
                                        if (ileane)
                                            Toast.makeText(cnx, "Файл загружен в Downloads/" + loadedFile.getName() + ".jpg",
                                                    Toast.LENGTH_LONG).show();
                                        download.setFinish();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(FileLoadRequest request, Throwable t) {
                                }
                            });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final PosTopViewHolder holder, int position) {

        try {
            holder.iv_piture.setImageResource(R.drawable.image_no);
            final Post model = posts.get(position);
            holder.post_rate.setText(String.valueOf(position + 1));
            final String postKey = model.keyRec;
            final boolean[] like = {false};
            final int[] likes = {model.likes_count};

            holder.youtube.setVisibility(View.GONE);

            holder.lvHeaderPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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


                        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                holder.downloadButton.setIndeterminate();
                                saveMemas(uri, holder.downloadButton);
                            }
                        });


                        holder.share_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareImage(uri);
                            }
                        });

                    }
                });
            }

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // [START post_stars_transaction]
    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
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
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public LinkedList<Post> getPosts() {
        return posts;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public LinkedList<String> getKeys() {
        return keys;
    }

}
